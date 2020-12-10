package com.gitee.search.action;

import com.gitee.search.server.Request;
import com.gitee.search.server.Response;
import com.gitee.search.server.StaticFileService;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.stream.Stream;

/**
 * Bind url to action
 * @author Winter Lau<javayou@gmail.com>
 */
public class ActionExecutor {

    private final static Logger log = LoggerFactory.getLogger(ActionExecutor.class);

    private final static String DEFAULT_ACTION_CLASS = "Index"; //默认的Action类名
    private final static String DEFAULT_ACTION_METHOD = "index";//Action默认的方法名

    /**
     * 根据请求的 URL 进行相应处理，并返回执行结果
     * @param request
     * @return
     */
    public final static Response execute(Request request) throws ActionException {

        if(StaticFileService.isStatic(request)) //处理静态文件
            return Response.file(request.getPath());

        String[] paths = Stream.of(request.getPath().split("/")).filter(p -> p.length() > 0).toArray(String[]::new);
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
                throw new ActionException(HttpResponseStatus.NOT_FOUND, request.getPath());
        }

        try {
            return invokeActionMethod(actionMethod, request);
        } catch (InvocationTargetException e) {
            if(e.getCause() instanceof ActionException)
                throw (ActionException)e.getCause();
            throw new ActionException(HttpResponseStatus.INTERNAL_SERVER_ERROR, null, e.getCause());
        } catch (NoSuchMethodException e) {
            throw new ActionException(HttpResponseStatus.NOT_FOUND, request.getPath());
        } catch (IllegalAccessException e) {
            throw new ActionException(HttpResponseStatus.FORBIDDEN, request.getPath());
        } catch(Exception t) {
            throw new ActionException(HttpResponseStatus.INTERNAL_SERVER_ERROR, null, t);
        }
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

}
