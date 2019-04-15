
package org.apache.ibatis.reflection.invoker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.ibatis.reflection.Reflector;


/**
 *     实现 Invoker 接口，指定方法的调用器
 */
public class MethodInvoker implements Invoker {


  // 类型
  private final Class<?> type;

  // 指定方法
  private final Method method;

  public MethodInvoker(Method method) {
    this.method = method;
    // 参数为1时  一般是 setting 方法，设置 type 为方法参数[0]
    if (method.getParameterTypes().length == 1) {
      type = method.getParameterTypes()[0];
    } else {

      //  否则，一般是 getting 方法，设置 type 为返回类型
      type = method.getReturnType();
    }
  }

  // 执行指定方法
  @Override
  public Object invoke(Object target, Object[] args) throws IllegalAccessException, InvocationTargetException {
    try {
      return method.invoke(target, args);
    } catch (IllegalAccessException e) {
      if (Reflector.canControlMemberAccessible()) {
        method.setAccessible(true);
        return method.invoke(target, args);
      } else {
        throw e;
      }
    }
  }

  @Override
  public Class<?> getType() {
    return type;
  }
}
