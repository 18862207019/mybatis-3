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

public class BadSubject {
  /**
   *   id     INT NOT NULL,
   *   name   VARCHAR(20),
   *   age    INT NOT NULL,
   *   height INT,
   *   weight INT,
   *   active BIT,   这俩是没有的
   *   dt     TIMESTAMP    这俩是么有的
   */
  private final int id;
  private final String name;
  private final int age;
  private final Height height;  //这个类型是Height
  private final Double weight;   //这个类型是weight

  //height 方法参数的类型是 Height ，而不是 Integer 。因为 MyBatis 无法识别 Height 类，所以会创建 BadSubject 对象报错。
  public BadSubject(final int id, final String name, final int age, final Height height, final Double weight) {
    this.id = id;
    this.name = name;
    this.age = age;
    this.height = height;
    this.weight = weight == null ? 0 : weight;
  }

  private class Height {

  }
}
