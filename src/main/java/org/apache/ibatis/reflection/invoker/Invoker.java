package org.apache.ibatis.reflection.invoker;

import java.lang.reflect.InvocationTargetException;

/**
 * 调用者接口
 */
public interface Invoker {

  /**
   * 执行一次调用。而具体调用什么方法，由子类来实现。
   *
   * @param target 目标
   * @param args 参数
   * @return 结果
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  Object invoke(Object target, Object[] args) throws IllegalAccessException, InvocationTargetException;

  /**
   *
   * @return 类对象
   */
  Class<?> getType();
}
