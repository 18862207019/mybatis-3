package org.apache.ibatis.reflection.wrapper;

import java.util.List;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.property.PropertyTokenizer;

/**
 *      对象包装器接口，基于 MetaClass 工具类，定义对指定对象的各种操作。
 *      或者可以说，ObjectWrapper 是 MetaClass 的指定类的具象化
 */
public interface ObjectWrapper {

  /**
   * 获得值
   *
   * @param prop PropertyTokenizer 对象，相当于键
   * @return 值
   */
  Object get(PropertyTokenizer prop);

  /**
   * 设置值
   *
   * @param prop PropertyTokenizer 对象，相当于键
   * @param value 值
   */
  void set(PropertyTokenizer prop, Object value);

  /**
   * {@link MetaClass#findProperty(String, boolean)}
   */
  String findProperty(String name, boolean useCamelCaseMapping);

  /**
   * {@link MetaClass#getGetterNames()}
   */
  String[] getGetterNames();

  /**
   * {@link MetaClass#getSetterNames()}
   */
  String[] getSetterNames();

  /**
   * {@link MetaClass#getSetterType(String)}
   */
  Class<?> getSetterType(String name);

  /**
   * {@link MetaClass#getGetterType(String)}
   */
  Class<?> getGetterType(String name);

  /**
   * {@link MetaClass#hasSetter(String)}
   */
  boolean hasSetter(String name);

  /**
   * {@link MetaClass#hasGetter(String)}
   */
  boolean hasGetter(String name);


  /**
   * {@link MetaObject#forObject(Object, ObjectFactory, ObjectWrapperFactory, ReflectorFactory)}
   */
  MetaObject instantiatePropertyValue(String name, PropertyTokenizer prop, ObjectFactory objectFactory);

  /**
   * 是否为集合
   */
  boolean isCollection();

  /**
   * 添加元素到集合
   */
  void add(Object element);

  /**
   * 添加多个元素到集合
   */
  <E> void addAll(List<E> element);

}
