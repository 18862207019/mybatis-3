package org.apache.ibatis.reflection.property;

import java.lang.reflect.Field;

import org.apache.ibatis.reflection.Reflector;

/**
 * 属性复制器
 */
public final class PropertyCopier {

  private PropertyCopier() {
  }

  // 将 sourceBean 的属性，复制到 destinationBean 中
  public static void copyBeanProperties(Class<?> type, Object sourceBean, Object destinationBean) {
    Class<?> parent = type;

    // // 循环，从当前类开始，不断复制到父类，直到父类不存在
    while (parent != null) {

      //获得当前 parent 类定义的属性
      final Field[] fields = parent.getDeclaredFields();
      for (Field field : fields) {
        try {
          try {

            // 从 sourceBean 中，复制到 destinationBean 去
            field.set(destinationBean, field.get(sourceBean));

          } catch (IllegalAccessException e) {
            if (Reflector.canControlMemberAccessible()) {
              field.setAccessible(true);
              field.set(destinationBean, field.get(sourceBean));
            } else {
              throw e;
            }
          }
        } catch (Exception e) {
        }
      }

      // 获得父类
      parent = parent.getSuperclass();
    }
  }

}
