package org.apache.ibatis.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.binding.MapperMethod.ParamMap;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

// 参数名解析器
public class ParamNameResolver {

  private static final String GENERIC_NAME_PREFIX = "param";


  private final SortedMap<Integer, String> names;

  /**
   * 是否有 {@link Param} 注解的参数
   */
  private boolean hasParamAnnotation;

  public ParamNameResolver(Configuration config, Method method) {
    final Class<?>[] paramTypes = method.getParameterTypes();
    final Annotation[][] paramAnnotations = method.getParameterAnnotations();
    final SortedMap<Integer, String> map = new TreeMap<>();
    int paramCount = paramAnnotations.length;
    // get names from @Param annotations
    for (int paramIndex = 0; paramIndex < paramCount; paramIndex++) {
      if (isSpecialParameter(paramTypes[paramIndex])) {
        // 忽略，如果是特殊参数
        continue;
      }
      String name = null;

      // 首先，从 @Param 注解中获取参数
      for (Annotation annotation : paramAnnotations[paramIndex]) {
        if (annotation instanceof Param) {
          hasParamAnnotation = true;
          name = ((Param) annotation).value();
          break;
        }
      }
      if (name == null) {
        // 其次，获取真实的参数名
        if (config.isUseActualParamName()) {   //默认开启
          name = getActualParamName(method, paramIndex);
        }
        // 最差，使用 map 的顺序，作为编号
        if (name == null) {
          name = String.valueOf(map.size());
        }
      }

      // 添加到 map 中
      map.put(paramIndex, name);
    }

    // 构建不可变集合
    names = Collections.unmodifiableSortedMap(map);
  }

  private String getActualParamName(Method method, int paramIndex) {
    return ParamNameUtil.getParamNames(method).get(paramIndex);
  }

  private static boolean isSpecialParameter(Class<?> clazz) {
    return RowBounds.class.isAssignableFrom(clazz) || ResultHandler.class.isAssignableFrom(clazz);
  }

  public String[] getNames() {
    return names.values().toArray(new String[0]);
  }

// 获得参数名与值的映射
  public Object getNamedParams(Object[] args) {
    final int paramCount = names.size();

    // 无参数，则返回 null
    if (args == null || paramCount == 0) {
      return null;

      // 只有一个非注解的参数，直接返回首元素
    } else if (!hasParamAnnotation && paramCount == 1) {
      return args[names.firstKey()];
    } else {

      // 集合。
      // 组合 1 ：KEY：参数名，VALUE：参数值
      // 组合 2 ：KEY：GENERIC_NAME_PREFIX + 参数顺序，VALUE ：参数值
      final Map<String, Object> param = new ParamMap<>();
      int i = 0;

      // 遍历 names 集合
      for (Map.Entry<Integer, String> entry : names.entrySet()) {

        // 组合 1 ：添加到 param 中
        param.put(entry.getValue(), args[entry.getKey()]);
        // 组合 2 ：添加到 param 中
        final String genericParamName = GENERIC_NAME_PREFIX + String.valueOf(i + 1);

        if (!names.containsValue(genericParamName)) {
          param.put(genericParamName, args[entry.getKey()]);
        }
        i++;
      }
      return param;
    }
  }
}
