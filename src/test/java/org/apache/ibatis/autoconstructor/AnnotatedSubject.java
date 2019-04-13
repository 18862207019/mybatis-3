/**
 *    Copyright 2009-2017 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.autoconstructor;

import org.apache.ibatis.annotations.AutomapConstructor;

/**
 *  对应数据库表格 subject
 */
public class AnnotatedSubject {

  /**
   *   id     INT NOT NULL,
   *   name   VARCHAR(20),
   *   age    INT NOT NULL,
   *   height INT,
   *   weight INT,
   *   active BIT,   这个字段是没有的
   *   dt     TIMESTAMP   这个字段是没有的
   */
  private final int id;
  private final String name;
  private final int age;
  private final int height;
  private final int weight;

  public AnnotatedSubject(final int id, final String name, final int age, final int height, final int weight) {
    this.id = id;
    this.name = name;
    this.age = age;
    this.height = height;
    this.weight = weight;
  }

  /**
   *     @AutomapConstructor 注解,表示在MyBatis查询后,在创建AnnotatedSubject 类的时候  使用该构造器
   * @param id
   * @param name
   * @param age
   * @param height    Integer 类型
   * @param weight    Integer 类型
   */
  @AutomapConstructor
  public AnnotatedSubject(final int id, final String name, final int age, final Integer height, final Integer weight) {
    this.id = id;
    this.name = name;
    this.age = age;
    this.height = height == null ? 0 : height;
    this.weight = weight == null ? 0 : weight;
  }
}
