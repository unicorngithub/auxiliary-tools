package com.auxiliary.interfaces.log.advice;

import com.auxiliary.interfaces.log.config.InterfacesDocumentPreperties;
import com.auxiliary.interfaces.log.dto.AuxiliaryDocumentBeanDto;
import com.auxiliary.interfaces.log.dto.AuxiliaryDocumentMethodDto;
import com.auxiliary.interfaces.log.dto.AuxiliaryDocumentUrlDto;
import com.auxiliary.interfaces.log.enums.ParamsType;
import com.auxiliary.interfaces.log.interfaces.AuxiliaryAnalysisDocument;
import com.auxiliary.interfaces.log.utils.LogUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 辅助工具-接口文档生成
 *
 * @author Guo's
 */
public class InterfacesDocumentAdvice {

    private final String SEPARATOR = "-----------------------------------------------------------------------------------";
    private final InterfacesDocumentPreperties properties;
    private final ApplicationContext application;


    public InterfacesDocumentAdvice(InterfacesDocumentPreperties properties, ApplicationContext application) {
        this.properties = properties;
        this.application = application;
    }

    /**
     * 文档输出
     */
    public void printDocument() {
        String[] beanDefinitionNames = application.getBeanDefinitionNames();
        if (beanDefinitionNames.length == 0) return;

        System.out.println(SEPARATOR);
        // 获取指定包下所有类
        Collection<String> specifyClass = getSpecifyClass();
        // 获取所有Bean名
        Collection<AuxiliaryDocumentBeanDto> beanDtoList = getBeanNames(specifyClass);

        // 数据处理
        beanDtoList.forEach(dto -> {
            Object bean = application.getBean(dto.getBeanName());
            dto.setClas(bean.getClass());
            // 获取注解
            AuxiliaryAnalysisDocument classObject = AnnotationUtils.findAnnotation(bean.getClass(), AuxiliaryAnalysisDocument.class);
            // 类存在注解
            if (null != classObject) {
                Method[] methods = ReflectionUtils.getDeclaredMethods(bean.getClass());
                Arrays.stream(methods).forEach(m -> {
                    RequestMapping annotation = AnnotationUtils.findAnnotation(m, RequestMapping.class);
                    if (annotation != null) {
                        printDocument(dto.setMethod(m));
                    }
                });
            }
            // 类无注解，遍历方法
            else {
                // 获取所有方法
                Method[] methods = ReflectionUtils.getDeclaredMethods(bean.getClass());
                Arrays.stream(methods).forEach(m -> {
                    AuxiliaryAnalysisDocument annotation = AnnotationUtils.findAnnotation(m, AuxiliaryAnalysisDocument.class);
                    if (null != annotation) {
                        printDocument(dto.setMethod(m));
                    }
                });
            }
        });
    }

    private void printDocument(AuxiliaryDocumentBeanDto auxiliaryBeanDto) {
        StringBuffer documentLog = LogUtils.appendLogln(SEPARATOR);
        LogUtils.appendln(documentLog, "解析方法[" + auxiliaryBeanDto.getClassName() + " -> " + auxiliaryBeanDto.getMethod().getName() + "]");
        try {
            // 接口地址
            AuxiliaryDocumentUrlDto urlDto = documentUrl(auxiliaryBeanDto);
            String requestMethod = Arrays.stream(urlDto.getMethod()).map(RequestMethod::name).collect(Collectors.joining("/"));
            LogUtils.appendln(documentLog, "接口：" + (StringUtils.isBlank(requestMethod) ? "ALL" : requestMethod) + " & " + urlDto.getUrl());
            // 接口入参
            Collection<String> documentParams = documentParams(auxiliaryBeanDto);
            LogUtils.appendln(documentLog, "入参：");
            documentParams.stream().filter(e -> documentParams.size() > 1 && "<无>".equals(e) ? false : true).forEach(e -> LogUtils.appendln(documentLog, e));
            // 接口出参
            Collection<String> documentResponse = documentResponse(auxiliaryBeanDto);
            LogUtils.appendln(documentLog, "出参：");
            documentResponse.stream().filter(e -> documentParams.size() > 1 && "<无>".equals(e) ? false : true).forEach(e -> LogUtils.appendln(documentLog, e));
        } catch (Exception ex) {
            if (properties.isDebug()) {
                ex.printStackTrace();
            }
        }
        System.out.print(documentLog.toString());
    }


    /************************************************************************************************************************ */
    /************************************************************************************************************************ */


    /**
     * 文档-URL
     *
     * @param auxiliaryBeanDto
     * @return
     */
    private AuxiliaryDocumentUrlDto documentUrl(AuxiliaryDocumentBeanDto auxiliaryBeanDto) {
        // 解析接口信息
        RequestMapping classAnnotation = AnnotationUtils.findAnnotation(auxiliaryBeanDto.getClas(), RequestMapping.class);
        String[] classUrls = classAnnotation.value();
        RequestMapping methodAnnotation = AnnotationUtils.findAnnotation(auxiliaryBeanDto.getMethod(), RequestMapping.class);
        String[] urls = methodAnnotation.value();
        if (urls.length == 0) {
            re:
            for (RequestMethod requestMethod : methodAnnotation.method()) {
                switch (requestMethod) {
                    case GET: {
                        GetMapping mapper = AnnotationUtils.findAnnotation(auxiliaryBeanDto.getMethod(), GetMapping.class);
                        urls = mapper.value();
                    }
                    break re;
                    case POST: {
                        PostMapping mapper = AnnotationUtils.findAnnotation(auxiliaryBeanDto.getMethod(), PostMapping.class);
                        urls = mapper.value();
                    }
                    break re;
                    case PUT: {
                        PutMapping mapper = AnnotationUtils.findAnnotation(auxiliaryBeanDto.getMethod(), PutMapping.class);
                        urls = mapper.value();
                    }
                    break re;
                    case DELETE: {
                        DeleteMapping mapper = AnnotationUtils.findAnnotation(auxiliaryBeanDto.getMethod(), DeleteMapping.class);
                        urls = mapper.value();
                    }
                    break re;
                    case PATCH: {
                        PatchMapping mapper = AnnotationUtils.findAnnotation(auxiliaryBeanDto.getMethod(), PatchMapping.class);
                        urls = mapper.value();
                    }
                    break re;
                }
            }
        }
        String url1 = classUrls.length == 0 ? null : classUrls[0];
        String url2 = urls.length == 0 ? null : urls[0];
        return new AuxiliaryDocumentUrlDto().setUrl(initURL(url1, url2)).setMethod(methodAnnotation.method());
    }

    /**
     * 文档-入参
     *
     * @param auxiliaryBeanDto
     * @return
     */
    private Collection<String> documentParams(AuxiliaryDocumentBeanDto auxiliaryBeanDto) {
        Set<String> set = new LinkedHashSet<>();
        // 获取参数
        Parameter[] parameters = getMethodParameters(auxiliaryBeanDto.getClas(), auxiliaryBeanDto.getMethod().getName());
        if (parameters.length == 0) {
            set.add("<无>");
        } else {
            Arrays.stream(parameters).forEach(e -> {
                String out = getParameterValue(e.getName(), e.getType(), 1, ParamsType.REQUEST);
                if (StringUtils.isBlank(out)) {
                    set.add("<无>");
                } else {
                    set.add(out);
                }
            });
        }
        return set;
    }

    /**
     * 文档-出参
     *
     * @param auxiliaryBeanDto
     * @return
     */
    private Collection<String> documentResponse(AuxiliaryDocumentBeanDto auxiliaryBeanDto) {
        Collection<String> list = new ArrayList<>();
        // 获取返回值
        Class responseClass = auxiliaryBeanDto.getMethod().getReturnType();
        String out = getParameterValue(null, responseClass, 0, ParamsType.RESPONSE);
        if (StringUtils.isBlank(out)) {
            String name = responseClass.getName();
            list.add("<" + name + ">");
        } else {
            String[] split = out.replace("\r", "").split("\n");
            for (int i = 0; i < split.length; i++) {
                list.add(split[i]);
            }
        }
        return list;
    }


    /************************************************************************************************************************ */
    /************************************************************************************************************************ */


    /**
     * 获取指定包下所有类
     *
     * @return
     */
    private Collection<String> getSpecifyClass() {
        Collection<String> beanSet = new ArrayList<>();
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        try {
            String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                    ClassUtils.convertClassNameToResourcePath(properties.getPackages()) + properties.getPattern();
            Resource[] resources = resourcePatternResolver.getResources(pattern);
            MetadataReaderFactory readerfactory = new CachingMetadataReaderFactory(resourcePatternResolver);
            for (Resource resource : resources) {
                // 用于读取类信息
                MetadataReader reader = readerfactory.getMetadataReader(resource);
                // 扫描到的class
                String classname = reader.getClassMetadata().getClassName();
                beanSet.add(classname);
            }
        } catch (Exception ex) {
            if (properties.isDebug()) {
                ex.printStackTrace();
            }
        }
        return beanSet;
    }

    /**
     * 获取所有Bean名
     *
     * @param collections
     * @return
     */
    private Collection<AuxiliaryDocumentBeanDto> getBeanNames(Collection<String> collections) {
        Collection<AuxiliaryDocumentBeanDto> beanDtoList = new ArrayList<>();
        String[] beanDefinitionNames = application.getBeanDefinitionNames();
        collections.stream().forEach(e -> {
            String className;
            int index = e.lastIndexOf(".");
            if (index > 0) {
                className = e.substring(index + 1);
            } else {
                className = e;
            }
            // 判断是否为大写
            String beanName = className;
            if (className.length() > 1) {
                if (!Character.isUpperCase(className.charAt(1))) {
                    beanName = firstToLowerCase(className);
                }
            } else {
                if (!Character.isUpperCase(className.charAt(0))) {
                    beanName = firstToLowerCase(className);
                }
            }
            for (int i = 0; i < beanDefinitionNames.length; i++) {
                if (beanDefinitionNames[i].equals(beanName)) {
                    beanDtoList.add(new AuxiliaryDocumentBeanDto().setBeanName(beanDefinitionNames[i]).setClassName(className));
                }
            }
        });
        return beanDtoList;
    }


    /************************************************************************************************************************ */
    /************************************************************************************************************************ */


    /**
     * 获取方法入参对象
     *
     * @param clas
     * @param methodName
     * @return
     */
    private Parameter[] getMethodParameters(Class clas, String methodName) {
        Class clazz = null;

        String classForName;
        if (clas.getName().indexOf("$") > 0) {
            classForName = clas.getName().substring(0, clas.getName().indexOf("$"));
        } else {
            classForName = clas.getName();
        }
        try {
            clazz = Class.forName(classForName);
        } catch (ClassNotFoundException e) {
            if (properties.isDebug()) {
                e.printStackTrace();
            }
        }
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (methodName.equals(method.getName())) {
                Parameter[] parameters = method.getParameters();
                return parameters;
            }
        }
        return new Parameter[0];
    }

    /**
     * 生成对象赋值
     *
     * @param clas
     * @return
     */
    private String getParameterValue(String name, Class clas, Integer index, ParamsType paramsType) {
        // 解析方法
        if (clas.isPrimitive() || clas.isArray() || verification(clas,
                Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class,
                Float.class, Double.class, String.class, BigDecimal.class, MultipartFile.class, Date.class)) {
            return StringUtils.isNotBlank(name) ? name + "::" + clas.getTypeName() : clas.getTypeName();
        }
        if (clas == HttpServletRequest.class || clas == HttpServletResponse.class) {
            return null;
        } else if (clas.isInterface()) {
            if (paramsType == ParamsType.REQUEST && properties.isDebug()) {
                System.err.println(String.format("接口类未解析，字段名=%s, 解析类型=%s", name, clas.getTypeName()));
            }
            return null;
        }

        Object obj = null;
        try {
            obj = clas.newInstance();
        } catch (Exception ex) {
            if (properties.isDebug()) {
                System.err.println(String.format("Class.newInstance()无法解析，字段名=%s, 解析类型=%s", name, clas.getTypeName()));
            }
        }
        // 过滤对象
        if (null == obj) return null;
        if (obj instanceof MultipartFile) return "sss";
        if (obj instanceof HttpServletRequest) return null;
        if (obj instanceof List) return "List[]";
        if (obj instanceof Map) return "Map{}";

        // 解析对象
        StringBuffer sb = new StringBuffer();
        List<AuxiliaryDocumentMethodDto> methods = getFieldSetMethod(clas);
        if (StringUtils.isNotBlank(name)) {
            sb.append(name + ":\r\n");
        }
        for (int i = 0; i < methods.size(); i++) {
            String str = getParameterValueVaules(methods.get(i), index, paramsType);
            if (StringUtils.isNotBlank(str)) {
                sb.append(str + "\r\n");
            }
        }
        if (sb.length() > 2) {
            sb.setLength(sb.length() - 2);
        }
        return sb.toString();
    }

    private String getParameterValueVaules(AuxiliaryDocumentMethodDto beanMethodDto, Integer index, ParamsType paramsType) {
        if (index++ > 3) {
            return null;
        }
        Class<?> returnType = beanMethodDto.getField().getType();
        String parameterValue = getParameterValue(beanMethodDto.getName(), returnType, index, paramsType);
        StringBuffer sb = new StringBuffer();
        for (int i = 1; i < index; i++) {
            sb.append("    ");
        }
        sb.append(parameterValue);
        return sb.toString();
    }

    private List<AuxiliaryDocumentMethodDto> getFieldSetMethod(Class clas) {
        List<AuxiliaryDocumentMethodDto> set = new ArrayList<>();
        Field[] fields = clas.getDeclaredFields();
        Method[] methods = clas.getMethods();
        Arrays.stream(methods).forEach(e -> {
            if (!(e.getName().length() > 3 && "set".equals(e.getName().substring(0, 3)))) {
                return;
            }
            Map<String, Field> fieldsMap = Arrays.stream(fields).collect(Collectors.toMap(Field::getName, Function.identity()));
            String name = firstToLowerCase(e.getName().substring(3));
            Field field = fieldsMap.get(name);
            if (null != field) {
                set.add(new AuxiliaryDocumentMethodDto().setName(name).setField(field));
            }
        });
        return set;
    }


    /************************************************************************************************************************ */
    /************************************************************************************************************************ */

    public static boolean verification(Class clas, Class... classes) {
        boolean bool = false;
        for (Class c : classes) {
            bool |= (clas == c);
        }
        return bool;
    }

    public static String firstToLowerCase(String param) {
        if (StringUtils.isBlank(param)) {
            return "";
        }
        return param.substring(0, 1).toLowerCase() + param.substring(1);
    }

    public static String initURL(String... urls) {
        StringBuffer sb = new StringBuffer();
        for (String url : urls) {
            if (StringUtils.isBlank(url)) {
                continue;
            }
            sb.append("/" + url);
        }
        return sb.toString().replace("//", "/");
    }

}
