package org.apache.ibatis.reflection.invoker;

import java.lang.reflect.Field;

import org.apache.ibatis.reflection.Reflector;

/**
 * 实现 Invoker 接口，获得 Field 调用者
 */
public class GetFieldInvoker implements Invoker {

  //  Field 对象
  private final Field field;

  public GetFieldInvoker(Field field) {
    this.field = field;
  }

  // 获得属性
  @Override
  public Object invoke(Object target, Object[] args) throws IllegalAccessException {
    try {
      return field.get(target);
    } catch (IllegalAccessException e) {
      if (Reflector.canControlMemberAccessible()) {
        field.setAccessible(true);
        return field.get(target);
      } else {
        throw e;
      }
    }
  }

  // 获得返回值类型
  @Override
  public Class<?> getType() {
    return field.getType();
  }
}
