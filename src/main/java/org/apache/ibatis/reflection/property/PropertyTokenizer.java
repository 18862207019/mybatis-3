package org.apache.ibatis.reflection.property;

import java.util.Iterator;

/**
 * 迭代器模式
 *
 *
 * 实现 Iterator 接口，属性分词器，支持迭代器的访问方式
 * 在访问 "order[0].item[0].name" 时，我们希望拆分成 "order[0]"、"item[0]"、"name" 三段，那么就可以通过 PropertyTokenizer 来实现。
 */
public class PropertyTokenizer implements Iterator<PropertyTokenizer> {

  //  当前字符串
  private String name;

  // 索引的 {@link #name} ，因为 {@link #name} 如果存在 {@link #index} 会被更
  private final String indexedName;

  /**
   * 编号。
   *
   * 对于数组 name[0] ，则 index = 0
   * 对于 Map map[key] ，则 index = key
   */
  private String index;

  /**
   * 剩余字符串
   */
  private final String children;

  public PropertyTokenizer(String fullname) {

    // <1> 初始化 name、children 字符串，使用 . 作为分隔
    int delim = fullname.indexOf('.');

    if (delim > -1) {
      name = fullname.substring(0, delim);
      children = fullname.substring(delim + 1);
    } else {
      name = fullname;
      children = null;
    }

    // <2> 记录当前 name
    indexedName = name;

  // 若存在 [ ，则获得 index ，并修改 name 。
    delim = name.indexOf('[');
    if (delim > -1) {
      index = name.substring(delim + 1, name.length() - 1);
      name = name.substring(0, delim);
    }
  }

  public String getName() {
    return name;
  }

  public String getIndex() {
    return index;
  }

  public String getIndexedName() {
    return indexedName;
  }


  public String getChildren() {
    return children;
  }

  // 判断是否有下一个元素
  @Override
  public boolean hasNext() {
    return children != null;
  }

  //  迭代获得下一个 PropertyTokenizer 对象
  @Override
  public PropertyTokenizer next() {
    return new PropertyTokenizer(children);
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Remove is not supported, as it has no meaning in the context of properties.");
  }
}
