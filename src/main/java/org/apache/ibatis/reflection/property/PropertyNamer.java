
package org.apache.ibatis.reflection.property;

import java.util.Locale;

import org.apache.ibatis.reflection.ReflectionException;

/**
 *  属性名相关的工具类方法
 */
public final class PropertyNamer {

  private PropertyNamer() {

  }


  //根据方法名，获得对应的属性名
  public static String methodToProperty(String name) {

    // is 方法
    if (name.startsWith("is")) {
      name = name.substring(2);
      // get 或者 set 方法
    } else if (name.startsWith("get") || name.startsWith("set")) {
      name = name.substring(3);
    } else {

      // 抛出 ReflectionException 异常，因为只能处理 is、set、get 方法
      throw new ReflectionException("Error parsing property name '" + name + "'.  Didn't start with 'is', 'get' or 'set'.");
    }

    // 首字母小写
    if (name.length() == 1 || (name.length() > 1 && !Character.isUpperCase(name.charAt(1)))) {
      name = name.substring(0, 1).toLowerCase(Locale.ENGLISH) + name.substring(1);
    }

    return name;
  }


  // 判断是否为 is、get、set 方法
  public static boolean isProperty(String name) {
    return name.startsWith("get") || name.startsWith("set") || name.startsWith("is");
  }
  //  判断是否为 get、is 方法
  public static boolean isGetter(String name) {
    return name.startsWith("get") || name.startsWith("is");
  }

  // 判断是否是set方法
  public static boolean isSetter(String name) {
    return name.startsWith("set");
  }

}
