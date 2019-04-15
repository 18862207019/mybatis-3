
package org.apache.ibatis.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

//参数名工具类，获得构造方法、普通方法的参数列表
public class ParamNameUtil {

  /**
   * 获得普通方法的参数列表
   *
   * @param method 普通方法
   * @return 参数集合
   */
  public static List<String> getParamNames(Method method) {
    return getParameterNames(method);
  }

  /**
   * 获得构造方法的参数列表
   *
   * @param constructor 构造方法
   * @return 参数集合
   */
  public static List<String> getParamNames(Constructor<?> constructor) {
    return getParameterNames(constructor);
  }

  private static List<String> getParameterNames(Executable executable) {
    return Arrays.stream(executable.getParameters()).map(Parameter::getName).collect(Collectors.toList());
  }

  private ParamNameUtil() {
    super();
  }
}
