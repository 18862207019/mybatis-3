package org.apache.ibatis.reflection;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Reflector 工厂接口，用于创建和缓存 Reflector 对象
 */
public class DefaultReflectorFactory implements ReflectorFactory {

  // 是否缓存
  private boolean classCacheEnabled = true;

  // Reflector 的缓存映射
  private final ConcurrentMap<Class<?>, Reflector> reflectorMap = new ConcurrentHashMap<>();

  public DefaultReflectorFactory() {
  }

  @Override
  public boolean isClassCacheEnabled() {
    return classCacheEnabled;
  }

  @Override
  public void setClassCacheEnabled(boolean classCacheEnabled) {
    this.classCacheEnabled = classCacheEnabled;
  }

  @Override
  public Reflector findForClass(Class<?> type) {
    // 开启缓存，则从 reflectorMap 中获取
    if (classCacheEnabled) {

    // 不存在，则进行创建
      return reflectorMap.computeIfAbsent(type, Reflector::new);

    // 关闭缓存，则创建 Reflector 对象
    } else {
      return new Reflector(type);
    }
  }

}
