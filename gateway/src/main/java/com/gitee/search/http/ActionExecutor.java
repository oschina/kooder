package com.gitee.search.http;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Bind url to action
 * @author Winter Lau<javayou@gmail.com>
 */
public class ActionExecutor {

    private final static Logger log = LoggerFactory.getLogger(ActionExecutor.class);

    private final static String ACTION_PACKAGE_NAME = "com.gitee.search.action";
    private final static String DEFAULT_ACTION_CLASS = "Index"; //默认的Action类名
    private final static String DEFAULT_ACTION_METHOD = "index";//Action默认的方法名

    private final static Map<String, Method> methods = new ConcurrentHashMap<>();

    /**
     * 根据请求的 URL 进行相应处理，并返回执行结果
     * @param context
     * @return
     */
    protected static void execute(RoutingContext context) {

        String uriPath = context.request().path();
        String[] paths = Stream.of(uriPath.split("/")).filter(p -> p.length() > 0).toArray(String[]::new);

        Method actionMethod = null;
        switch (paths.length) {
            case 0:
                actionMethod = findMethod(DEFAULT_ACTION_CLASS, DEFAULT_ACTION_METHOD);
                break;
            case 1:
                actionMethod = findMethod(paths[0], DEFAULT_ACTION_METHOD);
                if(actionMethod == null) {
                    actionMethod = findMethod(DEFAULT_ACTION_CLASS, paths[0]);
                }
                break;
            default:
                actionMethod = findMethod(paths[0], paths[1]);
                if(actionMethod == null) {
                    actionMethod = findMethod(paths[0], DEFAULT_ACTION_METHOD);
                    if(actionMethod == null) {
                        actionMethod = findMethod(DEFAULT_ACTION_CLASS, paths[0]);
                    }
                }
        }

        HttpServerResponse res = context.response();

        if(actionMethod == null) {
            //actionMethod = findActionMethod(DEFAULT_ACTION_CLASS, DEFAULT_ACTION_METHOD);
            //if(actionMethod == null)
            sendError(res, 404);
            return ;
        }

        invokeMethod(actionMethod, context);
    }

    /**
     * 调用 action 方法
     * @param actionMethod
     * @param context
     * @return
     */
    private static void invokeMethod(Method actionMethod, RoutingContext context) {
        boolean isStatic = Modifier.isStatic(actionMethod.getModifiers());
        Class actionClass = actionMethod.getDeclaringClass();
        HttpServerResponse response = context.response();
        try {
            Object targetObject = isStatic ? actionClass : actionClass.getDeclaredConstructor().newInstance();
            switch (actionMethod.getParameterCount()) {
                case 0:
                    actionMethod.invoke(targetObject);
                    break;
                case 1:
                    actionMethod.invoke(targetObject, context);
                    break;
                default:
                    throw new IllegalArgumentException(actionMethod.getName());
            }
        } catch (InvocationTargetException e) {
            log.error("Failed to invoke " + context.request().uri(), e.getCause());
            sendError(response, 500, e.getCause().getMessage());
        } catch (IllegalArgumentException e) {
            sendError(response, 400);
        } catch (IllegalAccessException e) {
            sendError(response, 403);
        } catch (NoSuchMethodException e) {
            sendError(response, 404);
        } catch (Throwable t) {
            log.error("Failed to invoke " + context.request().uri(), t);
            sendError(response, 500, t.getMessage());
        }

    }

    /**
     * 获取action的方法实例
     * @param className
     * @param methodName
     * @return
     */
    private final static Method findMethod(String className, String methodName) {
        String cacheKey = className+"."+methodName;
        return methods.computeIfAbsent(cacheKey, key -> {
            String newClassName = Character.toUpperCase(className.charAt(0)) + className.substring(1) + "Action";
            String fullClassName = ACTION_PACKAGE_NAME + "." + newClassName;
            try {
                Class actionClass = Class.forName(fullClassName);
                for(Method actionMethod : actionClass.getDeclaredMethods()){
                    if(actionMethod.getName().equals(methodName) && Modifier.isPublic(actionMethod.getModifiers())) {
                        //To check whether action method's parameters is ok
                        Class[] ptypes = actionMethod.getParameterTypes();
                        if(ptypes.length > 1)
                            continue ;
                        if(ptypes.length == 1 && !ptypes[0].equals(RoutingContext.class))
                            continue;
                        return actionMethod;
                    }
                }
            } catch (ClassNotFoundException e) {}
            return null;
        });
    }

    private static void sendError(HttpServerResponse res, int code, String...msg) {
        res.setStatusCode(code);
        if(msg != null && msg.length > 0)
            res.setStatusMessage(String.join("",msg));
        res.end();
    }

}
