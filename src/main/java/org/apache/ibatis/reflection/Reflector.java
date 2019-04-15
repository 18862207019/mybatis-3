package org.apache.ibatis.reflection;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.ReflectPermission;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.ibatis.reflection.invoker.GetFieldInvoker;
import org.apache.ibatis.reflection.invoker.Invoker;
import org.apache.ibatis.reflection.invoker.MethodInvoker;
import org.apache.ibatis.reflection.invoker.SetFieldInvoker;
import org.apache.ibatis.reflection.property.PropertyNamer;

/**
 * 反射器，每个 Reflector 对应一个类。Reflector 会缓存反射操作需要的类的信息，例如：构造方法、属性名、setting / getting 方法等等。
 */
public class Reflector {

  // 对应的类
  private final Class<?> type;

  // 可读属性数组
  private final String[] readablePropertyNames;

  // 可写属性数组
  private final String[] writablePropertyNames;

  // 属性对应的 setting 方法的映射。 key 为属性名称   value 为 Invoker 对象
  private final Map<String, Invoker> setMethods = new HashMap<>();

  // 属性对应的 getting  方法的映射。 key 为属性名称   value 为 Invoker 对象
  private final Map<String, Invoker> getMethods = new HashMap<>();

  // 属性对应的 setting 方法的方法参数类型的映射。{@link #setMethods}    key 为属性名称   value 为方法参数类型
  private final Map<String, Class<?>> setTypes = new HashMap<>();

  // 属性对应的 getting 方法的返回值类型的映射。{@link #getMethods}    key 为属性名称   value 为返回值的类型
  private final Map<String, Class<?>> getTypes = new HashMap<>();

  // 默认构造方法
  private Constructor<?> defaultConstructor;

  // 不区分大小写的属性集合
  private Map<String, String> caseInsensitivePropertyMap = new HashMap<>();

  // 构造器
  public Reflector(Class<?> clazz) {

    // 设置对应的类
    type = clazz;

    // <1> 初始化 defaultConstructor
    addDefaultConstructor(clazz);

    // <2> 初始化 getMethods 和 getTypes ，通过遍历 getting 方法
    addGetMethods(clazz);

    // <3>初始化 setMethods 和 setTypes ，通过遍历 setting 方法。
    addSetMethods(clazz);

    // <4>初始化 getMethods + getTypes 和 setMethods + setTypes ，通过遍历 fields 属性。
    addFields(clazz);

    // <5> 初始化 readablePropertyNames、writeablePropertyNames、caseInsensitivePropertyMap 属性
    readablePropertyNames = getMethods.keySet().toArray(new String[getMethods.keySet().size()]);
    writablePropertyNames = setMethods.keySet().toArray(new String[setMethods.keySet().size()]);

    for (String propName : readablePropertyNames) {
      caseInsensitivePropertyMap.put(propName.toUpperCase(Locale.ENGLISH), propName);
    }
    for (String propName : writablePropertyNames) {
      caseInsensitivePropertyMap.put(propName.toUpperCase(Locale.ENGLISH), propName);
    }
  }

  // <1> 初始化 defaultConstructor
  private void addDefaultConstructor(Class<?> clazz) {

    // 获得所有构造方法
    Constructor<?>[] consts = clazz.getDeclaredConstructors();

    // 遍历所有构造方法，查找无参的构造方法
    for (Constructor<?> constructor : consts) {

      // 判断无参的构造方法
      if (constructor.getParameterTypes().length == 0) {

        // 设置构造方法
        this.defaultConstructor = constructor;
      }
    }
  }

  // <2> 初始化 getMethods 和 getTypes ，通过遍历 getting 方法
  private void addGetMethods(Class<?> cls) {

    //  属性与其 getting 方法的映射。
    Map<String, List<Method>> conflictingGetters = new HashMap<>();

    //  获得所有方法
    Method[] methods = getClassMethods(cls);

    //  遍历所有方法
    for (Method method : methods) {

      //  参数大于 0 ，说明不是 getting 方法，忽略
      if (method.getParameterTypes().length > 0) {
        continue;
      }

      // 以 get 或 is 方法名开头，说明是 getting 方法
      String name = method.getName();
      if ((name.startsWith("get") && name.length() > 3)
          || (name.startsWith("is") && name.length() > 2)) {

        // 获得属性
        name = PropertyNamer.methodToProperty(name);

        // 添加到 conflictingGetters 中
        addMethodConflict(conflictingGetters, name, method);
      }
    }
    // 解决 getting 冲突方法
    resolveGetterConflicts(conflictingGetters);
  }

  //  解决get冲突的方法 最终,一个属性 ,只保留一个对应的方法
  private void resolveGetterConflicts(Map<String, List<Method>> conflictingGetters) {

    // 遍历每个属性，查找其最匹配的方法。因为子类可以覆写父类的方法，所以一个属性，可能对应多个 getting 方法
    for (Entry<String, List<Method>> entry : conflictingGetters.entrySet()) {

    //最匹配的方法
      Method winner = null;

      String propName = entry.getKey();
      for (Method candidate : entry.getValue()) {

        // winner 为空，说明 candidate 为最匹配的方法
        if (winner == null) {
          winner = candidate;
          continue;
        }

        // 基于返回类型比较
        Class<?> winnerType = winner.getReturnType();
        Class<?> candidateType = candidate.getReturnType();

        //  类型相同
        if (candidateType.equals(winnerType)) {

          //如果俩个人的返回值都一样。如果竞争者不是Boolean 就会抛出异常
            if (!boolean.class.equals(candidateType)) {
            throw new ReflectionException(
                "Illegal overloaded getter method with ambiguous type for property "
                    + propName + " in class " + winner.getDeclaringClass()
                    + ". This breaks the JavaBeans specification and can cause unpredictable results.");

            //如果竞争者是Boolean 则把胜利者淘汰 并且是is胜出  不是get胜出
          } else if (candidate.getName().startsWith("is")) {
            winner = candidate;
          }

          /**
           * class1.isAssignableFrom(class2) 判定此 Class 对象所表示的类或接口与指定的 Class 参数所表示的类或接口是否相同
           * ，或是否是其超类或超接口。如果是则返回 true；
           * 否则返回 false。如果该 Class 表示一个基本类型，且指定的 Class 参数正是该 Class 对象，则该方法返回 true；否则返回 false。
           */
          //判断winner 是否是其超类
        } else if (candidateType.isAssignableFrom(winnerType)) {

          //判断winner是否是其子类
        } else if (winnerType.isAssignableFrom(candidateType)) {
          winner = candidate;
        } else {
          // 返回类型冲突
          throw new ReflectionException(
              "Illegal overloaded getter method with ambiguous type for property "
                  + propName + " in class " + winner.getDeclaringClass()
                  + ". This breaks the JavaBeans specification and can cause unpredictable results.");
        }
      }

      // 添加到 getMethods 和 getTypes 中
      addGetMethod(propName, winner);
    }
  }


  // 添加到get方法中
  private void addGetMethod(String name, Method method) {

    // 判断是合理的属性名
    if (isValidPropertyName(name)) {

      // 添加到 getMethods 中
      getMethods.put(name, new MethodInvoker(method));
      Type returnType = TypeParameterResolver.resolveReturnType(method, type);

      // 添加到 getTypes 中
      getTypes.put(name, typeToClass(returnType));
    }
  }

  // 添加到set方法中
  private void addSetMethods(Class<?> cls) {

    // 属性与其set方法的映射
    Map<String, List<Method>> conflictingSetters = new HashMap<>();

    //获得所有方法
    Method[] methods = getClassMethods(cls);

    // 遍历
    for (Method method : methods) {
      String name = method.getName();

      // 方法以set开头
      if (name.startsWith("set") && name.length() > 3) {

        // 参数为1个
        if (method.getParameterTypes().length == 1) {

          // 获得属性
          name = PropertyNamer.methodToProperty(name);

          // 添加到 conflictingSetters 中
          addMethodConflict(conflictingSetters, name, method);
        }
      }
    }

    // 解决set冲突的方法
    resolveSetterConflicts(conflictingSetters);
  }

  // 添加到 conflictingGetters 中
  private void addMethodConflict(Map<String, List<Method>> conflictingMethods, String name, Method method) {

    // 若key对应的value为空，会将第二个参数的返回值存入并返回
    List<Method> list = conflictingMethods.computeIfAbsent(name, k -> new ArrayList<>());
    list.add(method);
  }

  // 解决set冲突的方法
  private void resolveSetterConflicts(Map<String, List<Method>> conflictingSetters) {

    // 遍历每个属性，查找其最匹配的方法。因为子类可以覆写父类的方法，所以一个属性，可能对应多个 setting 方法
    for (String propName : conflictingSetters.keySet()) {
      List<Method> setters = conflictingSetters.get(propName);

      // 获取当前成员变量的返回值的类型
      Class<?> getterType = getTypes.get(propName);
      Method match = null;
      ReflectionException exception = null;

      //遍历属性对应的set方法
      for (Method setter : setters) {

      //获取第一个参数的类型
        Class<?> paramType = setter.getParameterTypes()[0];

      // 和get类型相同直接使用
        if (paramType.equals(getterType)) {
          match = setter;
          break;
        }

        if (exception == null) {
          try {

            //选择一个更加匹配的
            match = pickBetterSetter(match, setter, propName);
          } catch (ReflectionException e) {
            match = null;
            exception = e;
          }
        }
      }
      if (match == null) {
        throw exception;
      } else {

        // 添加到setMethod和setTypes中
        addSetMethod(propName, match);
      }
    }
  }

  /**
   *
   * @param setter1   方法1
   * @param setter2   方法2
   * @param property  属性名
   * @return
   */
  private Method pickBetterSetter(Method setter1, Method setter2, String property) {
    if (setter1 == null) {
      return setter2;
    }

    // 选择更加匹配的
    Class<?> paramType1 = setter1.getParameterTypes()[0];
    Class<?> paramType2 = setter2.getParameterTypes()[0];
    if (paramType1.isAssignableFrom(paramType2)) {
      return setter2;
    } else if (paramType2.isAssignableFrom(paramType1)) {
      return setter1;
    }
    throw new ReflectionException("Ambiguous setters defined for property '" + property + "' in class '"
        + setter2.getDeclaringClass() + "' with types '" + paramType1.getName() + "' and '"
        + paramType2.getName() + "'.");
  }

  // 添加到 setMethods 和 setTypes
  private void addSetMethod(String name, Method method) {

    //判断方法的签名是否是有效的签名
    if (isValidPropertyName(name)) {
      setMethods.put(name, new MethodInvoker(method));
      Type[] paramTypes = TypeParameterResolver.resolveParamTypes(method, type);
      setTypes.put(name, typeToClass(paramTypes[0]));
    }
  }

  //  获取当前对象的class对象
  private Class<?> typeToClass(Type src) {
    Class<?> result = null;

    // 普通类型
    if (src instanceof Class) {
      result = (Class<?>) src;

      // 泛型类型，使用泛型
    } else if (src instanceof ParameterizedType) {
      result = (Class<?>) ((ParameterizedType) src).getRawType();
      // 泛型数组 ，使用泛型
    } else if (src instanceof GenericArrayType) {
      Type componentType = ((GenericArrayType) src).getGenericComponentType();

      // 普通类型
      if (componentType instanceof Class) {
        result = Array.newInstance((Class<?>) componentType, 0).getClass();

      } else {

        // 递归该方法 返回类
        Class<?> componentClass = typeToClass(componentType);
        result = Array.newInstance(componentClass, 0).getClass();
      }
    }
    // 都不符合 使用Object
    if (result == null) {
      result = Object.class;
    }
    return result;
  }

  // 初始化 getMethods + getTypes 和 setMethods + setTypes ，通过遍历 fields 属性。
  private void addFields(Class<?> clazz) {

    // 获取所有变量
    Field[] fields = clazz.getDeclaredFields();


    for (Field field : fields) {
      if (!setMethods.containsKey(field.getName())) {

        // 当前变量不是静态的 和   final修饰的
        int modifiers = field.getModifiers();
        if (!(Modifier.isFinal(modifiers) && Modifier.isStatic(modifiers))) {

          // 添加到 setMethods 和 setTypes 中
          addSetField(field);
        }
      }

      if (!getMethods.containsKey(field.getName())) {

        // 添加到 getMethods 和 getTypes 中
        addGetField(field);
      }
    }
    if (clazz.getSuperclass() != null) {

      //递归 处理父类
      addFields(clazz.getSuperclass());
    }
  }

  // 添加到 setMethods 和 setTypes 中
  private void addSetField(Field field) {

    // 判断属性是否合理
    if (isValidPropertyName(field.getName())) {

      //添加到setMethod中
      setMethods.put(field.getName(), new SetFieldInvoker(field));
      Type fieldType = TypeParameterResolver.resolveFieldType(field, type);

      //添加到setType中
      setTypes.put(field.getName(), typeToClass(fieldType));
    }
  }
  // 添加到 getMethods 和 getTypes 中

  private void addGetField(Field field) {

    // 判断方法名称是否合理
    if (isValidPropertyName(field.getName())) {

      //添加到getMethod中
      getMethods.put(field.getName(), new GetFieldInvoker(field));
      Type fieldType = TypeParameterResolver.resolveFieldType(field, type);

      //添加到getType中
      getTypes.put(field.getName(), typeToClass(fieldType));
    }
  }

  // 判断是否是合理的属性名
  private boolean isValidPropertyName(String name) {
    return !(name.startsWith("$") || "serialVersionUID".equals(name) || "class".equals(name));
  }

  // 获取当前类型的所有方法
  private Method[] getClassMethods(Class<?> cls) {

    // 每个方法签名与该方法的映射
    Map<String, Method> uniqueMethods = new HashMap<>();

    // 循环类  类的父类,类的父类的父类 ,知道找到Object为止
    Class<?> currentClass = cls;

    while (currentClass != null && currentClass != Object.class) {

      // 记录当前类定义的方法
      addUniqueMethods(uniqueMethods, currentClass.getDeclaredMethods());

      //  记录接口中定义的方法
      Class<?>[] interfaces = currentClass.getInterfaces();

      for (Class<?> anInterface : interfaces) {
        addUniqueMethods(uniqueMethods, anInterface.getMethods());
      }

      // 获得父类
      currentClass = currentClass.getSuperclass();
    }

    // 转换成Method数组返回
    Collection<Method> methods = uniqueMethods.values();
    return methods.toArray(new Method[methods.size()]);
  }

  //记录当前类所定义的所有方法
  private void addUniqueMethods(Map<String, Method> uniqueMethods, Method[] methods) {
    for (Method currentMethod : methods) {
      // 忽略Bridge方法   https://www.zhihu.com/question/54895701/answer/141623158  解释什么是Bridge
      if (!currentMethod.isBridge()) {

        //获得方法签名
        String signature = getSignature(currentMethod);

        // 当 uniqueMethods 不存在时，进行添加
        if (!uniqueMethods.containsKey(signature)) {

          // 添加到 uniqueMethods 中
          uniqueMethods.put(signature, currentMethod);
        }
      }
    }
  }

  // 获取方法签名
  private String getSignature(Method method) {

    StringBuilder sb = new StringBuilder();

    // 返回值类型
    Class<?> returnType = method.getReturnType();

    // 方法名
    if (returnType != null) {
      sb.append(returnType.getName()).append('#');
    }
    sb.append(method.getName());

    // 方法参数
    Class<?>[] parameters = method.getParameterTypes();
    for (int i = 0; i < parameters.length; i++) {
      if (i == 0) {
        sb.append(':');
      } else {
        sb.append(',');
      }
      sb.append(parameters[i].getName());
    }
    return sb.toString();
  }


  //判断，是否可以修改可访问性
  public static boolean canControlMemberAccessible() {
    try {
      SecurityManager securityManager = System.getSecurityManager();
      if (null != securityManager) {
        securityManager.checkPermission(new ReflectPermission("suppressAccessChecks"));
      }
    } catch (SecurityException e) {
      return false;
    }
    return true;
  }


  /**
   * 从这以下都是对属性进行访问
   * @return
   */
  public Class<?> getType() {
    return type;
  }

  public Constructor<?> getDefaultConstructor() {
    if (defaultConstructor != null) {
      return defaultConstructor;
    } else {
      throw new ReflectionException("There is no default constructor for " + type);
    }
  }

  public boolean hasDefaultConstructor() {
    return defaultConstructor != null;
  }

  public Invoker getSetInvoker(String propertyName) {
    Invoker method = setMethods.get(propertyName);
    if (method == null) {
      throw new ReflectionException("There is no setter for property named '" + propertyName + "' in '" + type + "'");
    }
    return method;
  }

  public Invoker getGetInvoker(String propertyName) {
    Invoker method = getMethods.get(propertyName);
    if (method == null) {
      throw new ReflectionException("There is no getter for property named '" + propertyName + "' in '" + type + "'");
    }
    return method;
  }


  public Class<?> getSetterType(String propertyName) {
    Class<?> clazz = setTypes.get(propertyName);
    if (clazz == null) {
      throw new ReflectionException("There is no setter for property named '" + propertyName + "' in '" + type + "'");
    }
    return clazz;
  }


  public Class<?> getGetterType(String propertyName) {
    Class<?> clazz = getTypes.get(propertyName);
    if (clazz == null) {
      throw new ReflectionException("There is no getter for property named '" + propertyName + "' in '" + type + "'");
    }
    return clazz;
  }


  public String[] getGetablePropertyNames() {
    return readablePropertyNames;
  }


  public String[] getSetablePropertyNames() {
    return writablePropertyNames;
  }


  public boolean hasSetter(String propertyName) {
    return setMethods.keySet().contains(propertyName);
  }


  public boolean hasGetter(String propertyName) {
    return getMethods.keySet().contains(propertyName);
  }

  public String findPropertyName(String name) {
    return caseInsensitivePropertyMap.get(name.toUpperCase(Locale.ENGLISH));
  }
}
