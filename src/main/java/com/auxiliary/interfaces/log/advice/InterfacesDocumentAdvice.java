package com.auxiliary.interfaces.log.advice;

import com.auxiliary.interfaces.log.config.InterfacesDocumentPreperties;
import com.auxiliary.interfaces.log.dto.AuxiliaryDocumentBeanDto;
import com.auxiliary.interfaces.log.dto.AuxiliaryDocumentMethodDto;
import com.auxiliary.interfaces.log.dto.AuxiliaryDocumentUrlDto;
import com.auxiliary.interfaces.log.enums.ParamsType;
import com.auxiliary.interfaces.log.interfaces.AuxiliaryAnalysisDocument;
import com.auxiliary.interfaces.log.utils.AddressUtils;
import com.auxiliary.interfaces.log.utils.LogUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.*;
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
        System.out.println(">>> 接口文档生成开始");
        // 获取指定包下所有类
        Collection<String> specifyClass = getSpecifyClass();
        // 获取所有Bean名
        Collection<AuxiliaryDocumentBeanDto> beanDtoList = getBeanNames(specifyClass);

        List<String> analysisInfo = new ArrayList<>();
        // 数据处理
        beanDtoList.forEach(dto -> {
            Object bean = application.getBean(dto.getBeanName());
            dto.setClas(bean.getClass());
            if (0 == properties.getPrintType()) {
                // 获取注解
                AuxiliaryAnalysisDocument classObject = AnnotationUtils.findAnnotation(bean.getClass(), AuxiliaryAnalysisDocument.class);
                // 类存在注解
                if (null != classObject) {
                    Method[] methods = ReflectionUtils.getDeclaredMethods(bean.getClass());
                    Arrays.stream(methods).forEach(m -> {
                        RequestMapping annotation = AnnotationUtils.findAnnotation(m, RequestMapping.class);
                        if (annotation != null) {
                            String documentStr = printDocument(dto.setMethod(m));
                            analysisInfo.add(documentStr);
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
                            String documentStr = printDocument(dto.setMethod(m));
                            analysisInfo.add(documentStr);
                        }
                    });
                }
            } else {
                Method[] methods = ReflectionUtils.getDeclaredMethods(bean.getClass());
                Arrays.stream(methods).forEach(m -> {
                    RequestMapping annotation = AnnotationUtils.findAnnotation(m, RequestMapping.class);
                    if (annotation != null) {
                        String documentStr = printDocument(dto.setMethod(m));
                        analysisInfo.add(documentStr);
                    }
                });
            }
        });
        if (CollectionUtils.isEmpty(analysisInfo)) {
            System.out.println("未生成文档，如需生成文档信息请在接口上添加注解 @AuxiliaryAnalysisDocument");
        }
    }

    private String printDocument(AuxiliaryDocumentBeanDto auxiliaryBeanDto) {
        StringBuffer documentLog = LogUtils.appendLogln(SEPARATOR);
        if (properties.isDebug()) {
            LogUtils.appendln(documentLog, "解析方法[" + auxiliaryBeanDto.getClassName() + " -> " + auxiliaryBeanDto.getMethod().getName() + "]");
        }
        try {
            // 接口地址
            AuxiliaryDocumentUrlDto urlDto = documentUrl(auxiliaryBeanDto);
            String requestMethod = Arrays.stream(urlDto.getMethod()).map(RequestMethod::name).collect(Collectors.joining("/"));
            LogUtils.appendln(documentLog, "接口：" + (StringUtils.isBlank(requestMethod) ? "ALL" : requestMethod) + " & " + webUrl() + urlDto.getUrl());
            // 接口入参
            Collection<String> documentParams = documentParams(auxiliaryBeanDto);
            LogUtils.appendln(documentLog, "入参：");
            documentParams.stream().filter(e -> documentParams.size() > 1 && ">>".equals(e) ? false : true).forEach(e -> LogUtils.appendln(documentLog, e));
            // 接口出参
            Collection<String> documentResponse = documentResponse(auxiliaryBeanDto);
            LogUtils.appendln(documentLog, "出参：");
            documentResponse.stream().filter(e -> documentParams.size() > 1 && ">>".equals(e) ? false : true).forEach(e -> LogUtils.appendln(documentLog, e));
        } catch (Exception ex) {
            if (properties.isDebug()) {
                ex.printStackTrace();
            }
        }
        documentLog.append(SEPARATOR).append("\n").append("\n");
        System.out.print(documentLog);
        return documentLog.toString();
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
        // 获取参数（直接获取auxiliaryBeanDto.getMethod().getParameters()中对象，无法获取参数实际名称）
        Parameter[] parameters = getMethodParameters(auxiliaryBeanDto.getClas(), auxiliaryBeanDto.getMethod());
        if (parameters.length == 0) {
            set.add(">>");
        } else {
            Arrays.stream(parameters).forEach(e ->
                    set.add(getParameterValue(e.getName(), e.getType(), 0, ParamsType.REQUEST, null)));
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
        // 获取对象类
        Class<?> responseClass = auxiliaryBeanDto.getMethod().getReturnType();
        // 解析出参
        String out = getParameterValue(null, responseClass, 0, ParamsType.RESPONSE, null);
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
            System.out.println(">>> 解析路径：" + pattern + "，扫描到的类数量：" + resources.length);
            Integer index = null;
            if (resources.length > 2000 && StringUtils.isBlank(properties.getPackages())) {
                System.out.println(">>> 扫描到的类数量过多，可配置指定扫描路径【auxiliary.document.packages】，以提高扫描效率。");
                index = resources.length / resources.length / 2000;
            }
            MetadataReaderFactory readerfactory = new CachingMetadataReaderFactory(resourcePatternResolver);
            for (int i = 0; i < resources.length; i++) {
                if (null != index && i > 0 && i % index == 0) {
                    BigDecimal bigDecimal = new BigDecimal(Float.valueOf(i) / resources.length * 100);
                    System.out.println(">>> 解析进度：" + bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP) + "%");
                }
                Resource resource = resources[i];
                // 用于读取类信息
                MetadataReader reader = readerfactory.getMetadataReader(resource);
                // 扫描到的class
                String classname = reader.getClassMetadata().getClassName();
                beanSet.add(classname);
            }
            System.out.println(">>> 解析完成！");
        } catch (Throwable ex) {
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
     * @param controllerMethod
     * @return
     */
    private Parameter[] getMethodParameters(Class<?> clas, Method controllerMethod) {
        String classForName;
        if (clas.getName().indexOf("$") > 0) {
            classForName = clas.getName().substring(0, clas.getName().indexOf("$"));
        } else {
            classForName = clas.getName();
        }

        Class<?> clazz = null;
        try {
            clazz = Class.forName(classForName);
        } catch (ClassNotFoundException e) {
            if (properties.isDebug()) {
                e.printStackTrace();
            }
        }

        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (areMethodsSame(controllerMethod, method)) {
                Parameter[] parameters = method.getParameters();
                return parameters;
            }
        }
        return new Parameter[0];
    }

    /**
     * 生成对象赋值
     *
     * @param name
     * @param clas
     * @param index
     * @param paramsType
     * @param genericityType 泛型类型（非泛型为Null）
     * @return
     */
    private String getParameterValue(String name, Class clas, Integer index, ParamsType paramsType, String genericityType) {
        // 解析方法
        if (clas == Void.class || clas == void.class) {
            return ">>";
        }
        if (clas == HttpServletRequest.class || clas == HttpServletResponse.class) {
            return ">>";
        }
        if (clas.isPrimitive() || clas.isArray() || verification(clas,
                Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class,
                Float.class, Double.class, String.class, BigDecimal.class, MultipartFile.class, Date.class)) {
            return ">> " + (StringUtils.isNotBlank(name) ? name + "::" + clas.getTypeName() : clas.getTypeName());
        }

        // 解析对象
        StringBuilder sb = new StringBuilder();
        List<AuxiliaryDocumentMethodDto> methods = getFieldSetMethod(clas);
        if (index > 0) {
            if (StringUtils.isNotBlank(name)) {
                if (StringUtils.isNotBlank(genericityType)) {
                    sb.append(">> " + name + "::" + genericityType + "\r\n");
                } else {
                    sb.append(">> " + name + "\r\n");
                }
            }
        }
        for (AuxiliaryDocumentMethodDto method : methods) {
            String str = getParameterValueVaules(method, index, paramsType);
            if (StringUtils.isNotBlank(str)) {
                sb.append(str + "\r\n");
            }
        }
        // 返回数据
        if (sb.length() > 2) {
            sb.setLength(sb.length() - 2);
        }
        return sb.toString();
    }

    private String getParameterValueVaules(AuxiliaryDocumentMethodDto beanMethodDto, Integer index, ParamsType paramsType) {
        if (index++ > 3) {
            return null;
        }
        Field field = beanMethodDto.getField();
        // 当前类型
        Class<?> returnType = field.getType();
        // 泛型类型
        Class<?> genericity = null;
        Type[] typeArguments = null;
        // 检查是否为 ParameterizedType
        if (field.getGenericType() instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            // 获取泛型类型的参数
            typeArguments = parameterizedType.getActualTypeArguments();
            for (Type typeArgument : typeArguments) {
                if (typeArgument instanceof Class) {
                    genericity = (Class) typeArgument;
                }
            }
        }

        StringBuilder builder = new StringBuilder();
        if (null == genericity && null == typeArguments) {
            String parameterValue = getParameterValue(beanMethodDto.getName(), returnType, index, paramsType, null);
            String value = stringBuilding(index, parameterValue);
            builder.append(value);
        } else if (null != typeArguments) {
            String parameterValue = getParameterValue(beanMethodDto.getName(), returnType, index, paramsType, null);
            String value = stringBuilding(index, parameterValue + "::" + typeArguments[0].getTypeName());
            builder.append(value);
        } else {
            // 泛型处理
            String genericityValue = getParameterValue(beanMethodDto.getName(), genericity, index, paramsType, returnType.getTypeName());
            String value = stringBuilding(index, genericityValue);
            builder.append(value);
        }
        return builder.toString();
    }

    /**
     * 获取当前类所有字段
     *
     * @param clazz
     * @return
     */
    private List<AuxiliaryDocumentMethodDto> getFieldSetMethod(Class clazz) {
        List<AuxiliaryDocumentMethodDto> set = new ArrayList<>();
        // 获取当前类和其父类的所有字段
        List<Field> fields = getAllFieldsIncludingSuperclasses(clazz);
        Method[] methods = clazz.getMethods();
        // 获取当前类的所有方法
        Arrays.stream(methods).forEach(method -> {
            if (!(method.getName().length() > 3 && "set".equals(method.getName().substring(0, 3)))) {
                return;
            }
            Map<String, Field> fieldsMap = fields.stream().collect(Collectors.toMap(Field::getName, Function.identity()));
            String name = firstToLowerCase(method.getName().substring(3));
            Field field = fieldsMap.get(name);
            if (null != field) {
                set.add(new AuxiliaryDocumentMethodDto().setName(name).setField(field));
            }
        });
        return set;
    }

    /**
     * 递归获取当前类和其父类的所有字段
     *
     * @param clazz
     * @return
     */
    private List<Field> getAllFieldsIncludingSuperclasses(Class<?> clazz) {
        List<Field> fields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null) {
            fields.addAll(getAllFieldsIncludingSuperclasses(superClass));
        }
        return fields;
    }


    /************************************************************************************************************************ */
    /************************************************************************************************************************ */

    /**
     * 判断两个方法是否一致
     *
     * @param m1
     * @param m2
     * @return
     */
    public boolean areMethodsSame(Method m1, Method m2) {
        // 方法名不同
        if (!m1.getName().equals(m2.getName())) {
            return false;
        }
        // 返回类型不同
        if (m1.getReturnType() != m2.getReturnType()) {
            return false;
        }
        return Arrays.equals(m1.getParameterTypes(), m2.getParameterTypes()); // 参数类型数组是否相同
    }

    private boolean verification(Class<?> clas, Class<?>... classes) {
        boolean bool = false;
        for (Class c : classes) {
            bool |= (clas == c);
        }
        return bool;
    }

    private String firstToLowerCase(String str) {
        if (StringUtils.isBlank(str)) {
            return "";
        }
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }

    private String initURL(String... urls) {
        StringBuffer sb = new StringBuffer();
        for (String url : urls) {
            if (StringUtils.isBlank(url)) {
                continue;
            }
            sb.append("/" + url);
        }
        return sb.toString().replace("//", "/");
    }

    private String webUrl() {
        Environment env = application.getEnvironment();
        String ip = AddressUtils.getLocalHostExactAddress().getHostAddress();
        String port = env.getProperty("server.port");
        String path = env.getProperty("server.servlet.context-path");
        return "http://" + ip + ":" + port + (StringUtils.isEmpty(path) ? "" : path);
    }

    private String stringBuilding(int index, String value) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < index; i++) {
            sb.append("  ");
        }
        sb.append(value);
        return sb.toString();
    }
}
