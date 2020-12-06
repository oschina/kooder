package com.gitee.search.action;

import io.netty.handler.codec.http.HttpResponseStatus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Bind url to action
 * @author Winter Lau<javayou@gmail.com>
 */
public class ActionExecutor {

    /**
     * 根据请求的 URL 进行相应处理，并返回执行结果
     * @param path
     * @param params
     * @param body
     * @return
     */
    public final static String execute(String path, Map<String, List<String>> params, StringBuilder body)
            throws ActionException
    {

        String className = null ,methodName = null;

        String[] paths = parsePath(path);
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
            return callActionMethod(actionClass, methodName, params, body);

        } catch (InvocationTargetException e) {
            if(e.getCause() instanceof ActionException)
                throw (ActionException)e.getCause();
            throw new ActionException(HttpResponseStatus.INTERNAL_SERVER_ERROR, null, e.getCause());
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new ActionException(HttpResponseStatus.NOT_FOUND, path);
        } catch (IllegalAccessException e) {
            throw new ActionException(HttpResponseStatus.FORBIDDEN, path);
        } catch(Throwable t) {
            throw new ActionException(HttpResponseStatus.INTERNAL_SERVER_ERROR, null, t);
        }
    }

    /**
     * 调用 action 方法
     * @param actionClass
     * @param methodName
     * @param params
     * @param body
     * @return
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    private static String callActionMethod(Class actionClass, String methodName, Map<String, List<String>> params, StringBuilder body)
            throws NoSuchMethodException,IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        Object result = null;
        Method actionMethod = getActionMethod(actionClass, methodName);
        switch(actionMethod.getParameterCount()){
            case 0:
                result = actionMethod.invoke(actionClass);
                break;
            case 1:
                result = actionMethod.invoke(actionClass, params);
                break;
            case 2:
                result = actionMethod.invoke(actionClass, params, body);
        }
        if(result == null)
            return null;
        if(result instanceof String)
            return (String)result;

        return result.toString();
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
