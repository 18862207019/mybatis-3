
package org.apache.ibatis.reflection;


public interface ReflectorFactory {


  // 是否缓存 Reflector 对象
  boolean isClassCacheEnabled();

  // 设置是否缓存 Reflector 对象
  void setClassCacheEnabled(boolean classCacheEnabled);

  //  获取 Reflector 对象
  Reflector findForClass(Class<?> type);
}