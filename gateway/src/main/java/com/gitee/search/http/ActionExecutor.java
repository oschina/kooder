package com.gitee.search.http;

import com.gitee.search.action.ActionException;
import com.gitee.search.server.Request;
import com.gitee.search.server.Response;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Bind url to action
 * @author Winter Lau<javayou@gmail.com>
 */
public class ActionExecutor {

    private final static Logger log = LoggerFactory.getLogger(ActionExecutor.class);

    private final static String DEFAULT_ACTION_CLASS = "Index"; //默认的Action类名
    private final static String DEFAULT_ACTION_METHOD = "index";//Action默认的方法名

    private final static Map<String, Method> methods = new ConcurrentHashMap<>();

    /**
     * 根据请求的 URL 进行相应处理，并返回执行结果
     * @param req
     * @return
     */
    public static void execute(HttpServerRequest req) throws ActionException {
        String uriPath = req.path();
        String[] paths = Stream.of(uriPath.split("/")).filter(p -> p.length() > 0).toArray(String[]::new);

        Method actionMethod = null;
        switch (paths.length) {
            case 0:
                actionMethod = findActionMethod(DEFAULT_ACTION_CLASS, DEFAULT_ACTION_METHOD);
                break;
            case 1:
                actionMethod = findActionMethod(paths[0], DEFAULT_ACTION_METHOD);
                if(actionMethod == null) {
                    actionMethod = findActionMethod(DEFAULT_ACTION_CLASS, paths[0]);
                }
                break;
            default:
                actionMethod = findActionMethod(paths[0], paths[1]);
                if(actionMethod == null) {
                    actionMethod = findActionMethod(paths[0], DEFAULT_ACTION_METHOD);
                    if(actionMethod == null) {
                        actionMethod = findActionMethod(DEFAULT_ACTION_CLASS, paths[0]);
                    }
                }
        }

        if(actionMethod == null) {
            //actionMethod = findActionMethod(DEFAULT_ACTION_CLASS, DEFAULT_ACTION_METHOD);
            //if(actionMethod == null)
            req.response().reset(404);
        }

        req.response().end();
    }

    /**
     * 调用 action 方法
     * @param actionMethod
     * @param request
     * @return
     */
    private static Response invokeActionMethod(Method actionMethod, Request request)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException
    {
        Object result = null;
        boolean isStatic = Modifier.isStatic(actionMethod.getModifiers());
        Class actionClass = actionMethod.getDeclaringClass();
        Object targetObject = isStatic?actionClass:actionClass.getDeclaredConstructor().newInstance();
        switch(actionMethod.getParameterCount()){
            case 0:
                result = actionMethod.invoke(targetObject);
                break;
            case 1:
                result = actionMethod.invoke(targetObject, request);
                break;
            default:
                throw new IllegalArgumentException(actionMethod.getName());
        }
        if(result == null)
            return null;

        if(result instanceof Response)
            return (Response) result;

        return Response.json((String)result);
    }

    /**
     * 获取action的方法实例
     * @param className
     * @param methodName
     * @return
     */
    private final static Method findActionMethod(String className, String methodName) {
        String cacheKey = className+"."+methodName;
        return methods.computeIfAbsent(cacheKey, new Function<String, Method>() {
            @Override
            public Method apply(String name) {
                String newClassName = Character.toUpperCase(className.charAt(0)) + className.substring(1) + "Action";
                String fullClassName = ActionExecutor.class.getPackage().getName() + "." + newClassName;
                try {
                    Class actionClass = Class.forName(fullClassName);
                    for(Method actionMethod : actionClass.getDeclaredMethods()){
                        if(actionMethod.getName().equals(methodName) && Modifier.isPublic(actionMethod.getModifiers()))
                            return actionMethod;
                    }
                } catch (ClassNotFoundException e) {}
                return null;
            }
        }::apply);
    }

}
