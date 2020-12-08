package com.gitee.search.action;

import com.gitee.search.server.Request;
import com.gitee.search.server.Response;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.stream.Stream;

/**
 * Bind url to action
 * @author Winter Lau<javayou@gmail.com>
 */
public class ActionExecutor {

    /**
     * 根据请求的 URL 进行相应处理，并返回执行结果
     * @param request
     * @return
     */
    public final static Response execute(Request request)
            throws ActionException
    {

        String className = null ,methodName = null;

        String[] paths = parsePath(request.getPath());
        switch (paths.length) {
            case 0:
                className = "Default";
                methodName = "index";
                break;
            case 1:
                className = paths[0];
                methodName = "index";
                break;
            case 2:
                className = paths[0];
                methodName = paths[1];
        }

        try {
            className = Character.toUpperCase(className.charAt(0)) + className.substring(1) + "Action";
            String fullclassName = ActionExecutor.class.getPackage().getName() + "." + className;
            Class actionClass = Class.forName(fullclassName);
            return callActionMethod(actionClass, methodName, request);

        } catch (InvocationTargetException e) {
            if(e.getCause() instanceof ActionException)
                throw (ActionException)e.getCause();
            throw new ActionException(HttpResponseStatus.INTERNAL_SERVER_ERROR, null, e.getCause());
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new ActionException(HttpResponseStatus.NOT_FOUND, request.getPath());
        } catch (IllegalAccessException e) {
            throw new ActionException(HttpResponseStatus.FORBIDDEN, request.getPath());
        } catch(Throwable t) {
            throw new ActionException(HttpResponseStatus.INTERNAL_SERVER_ERROR, null, t);
        }
    }

    /**
     * 调用 action 方法
     * @param actionClass
     * @param methodName
     * @param request
     * @return
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    private static Response callActionMethod(Class actionClass, String methodName, Request request)
            throws NoSuchMethodException,IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        Object result = null;
        Method actionMethod = getActionMethod(actionClass, methodName);
        switch(actionMethod.getParameterCount()){
            case 0:
                result = actionMethod.invoke(actionClass);
                break;
            case 1:
                result = actionMethod.invoke(actionClass, request);
                break;
            default:
                throw new NoSuchMethodException(methodName);
        }
        if(result == null)
            return null;

        if(result instanceof Response)
            return (Response) result;

        return Response.json((String)result);
    }

    /**
     * 获取action的方法实例
     * @param actionClass
     * @param methodName
     * @return
     * @throws NoSuchMethodException
     */
    private static Method getActionMethod(Class actionClass, String methodName) throws NoSuchMethodException {
        for(Method actionMethod : actionClass.getDeclaredMethods()){
            if(actionMethod.getName().equals(methodName))
                return actionMethod;
        }
        throw new NoSuchMethodException(methodName);
    }

    private static String[] parsePath(String uri) {
        return Stream.of(uri.split("/")).filter(p -> p.length() > 0).toArray(String[]::new);
    }
}
