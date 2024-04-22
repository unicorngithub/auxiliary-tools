package com.auxiliary.interfaces.log.advice;

import com.auxiliary.interfaces.log.config.AuxiliaryInterfacesLogProperties;
import com.auxiliary.interfaces.log.utils.AddressUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 辅助工具-启动打印日志
 *
 * @author Guo's
 */
@Slf4j
public class InterfacesRunLogAdvice {

    private final AuxiliaryInterfacesLogProperties properties;
    private final ApplicationContext application;

    public InterfacesRunLogAdvice(AuxiliaryInterfacesLogProperties properties, ApplicationContext application) {
        this.properties = properties;
        this.application = application;
    }

    public void printInterface() {
        try {
            Environment env = application.getEnvironment();
            String ip = AddressUtils.getLocalHostExactAddress().getHostAddress();
            String port = env.getProperty("server.port");
            String path = env.getProperty("server.servlet.context-path");
            String environmental = application.getEnvironment().getProperty("environmental.identification");
            if (StringUtils.isEmpty(path)) {
                path = "";
            }

            // 接口信息
            StringBuffer printInfo = new StringBuffer();
            printInfo.append("\n-------------------------------------------------------------------------\n\t" +
                    "Current operating environment: " + environmental + "\n\t" +
                    "Application is running! Access URLs:\n\t" +
                    "Local访问网址:  \t\thttp://localhost:" + port + path + "\n\t" +
                    "External访问网址: \thttp://" + ip + ":" + port + path + "\n\t" +
                    "-------------------------------------------------------------------------\n\t");
            // 获取所有链接
            printInfo.append("All request url:\n\t");
            List<String> urls = getAllUrl(application);
            for (String url : urls) {
                String[] methods = url.split("\\|");
                printInfo.append(complement(methods[0], 6) + " : http://" + ip + ":" + port + path + methods[1] + "\n\t");
            }
            printInfo.append("Total:" + urls.size() + "\n");
            printInfo.append("-------------------------------------------------------------------\n");
            // 打印数据
            log.info(printInfo.toString());
        } catch (Exception ex) {
        }
    }

    // 获取项目所有url
    private List<String> getAllUrl(ApplicationContext run) {
        RequestMappingHandlerMapping mapping = run.getBean(RequestMappingHandlerMapping.class);
        // 获取url与类和方法的对应信息
        Map<RequestMappingInfo, HandlerMethod> map = mapping.getHandlerMethods();
        List<String> urls = new ArrayList<>();
        map.forEach((requestMappingInfo, handlerMethod) -> {
            Set<String> patterns = new HashSet<>();
            // 获取请求映射信息中的直接路径集合，这些路径对应于具体的请求路径，不包含任何模式变量
            Set<String> directPaths = requestMappingInfo.getDirectPaths();
            // 获取请求映射信息中的模式值集合，这些值对应于包含模式变量的请求路径模式，如/{id}
            Set<String> patternValues = requestMappingInfo.getPatternValues();
            // 添加所有路径
            patterns.addAll(directPaths);
            patterns.addAll(patternValues);

            // 请求类型GET.POST.PUT...
            Set<RequestMethod> requestType = requestMappingInfo.getMethodsCondition().getMethods();
            for (String url : patterns) {
                String requestMethods = requestType.stream().map(RequestMethod::name).collect(Collectors.joining(","));
                urls.add(requestMethods + "|" + url);
            }
        });
        return urls.stream().sorted(Comparator.comparing(e -> e.split("\\|")[1])).distinct().collect(Collectors.toList());
    }

    /**
     * 补齐（左侧插入）
     *
     * @param value
     * @param index
     * @return
     */
    private String complement(String value, int index) {
        StringBuilder valueBuilder = new StringBuilder(value);
        while (valueBuilder.length() < index) {
            valueBuilder.insert(0, " ");
        }
        value = valueBuilder.toString();
        return value;
    }
}
