package org.apache.ibatis.reflection.factory;

import java.util.List;
import java.util.Properties;

/**
 * Object 工厂接口，用于创建指定类的对象。
 */
public interface ObjectFactory {

  // 设置 Properties
  void setProperties(Properties properties);


  // 创建指定类的对象，使用默认构造方法
  <T> T create(Class<T> type);


  /**
   * Creates a new object with the specified constructor and params.
   *
   * 创建指定类的对象，使用特定的构造方法
   *
   * @param type Object type
   * @param constructorArgTypes Constructor argument types 指定构造方法的参数列表
   * @param constructorArgs Constructor argument values 参数数组
   * @return 对象
   */
  <T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs);

  // 判断指定类是否为集合类
  <T> boolean isCollection(Class<T> type);

}
