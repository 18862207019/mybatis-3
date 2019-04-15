package org.apache.ibatis.exceptions;

// 继承 IbatisException 类，目前 MyBatis 真正的异常基类
@SuppressWarnings("deprecation")
public class PersistenceException extends IbatisException {

  private static final long serialVersionUID = -7537395265357977271L;

  public PersistenceException() {
    super();
  }

  public PersistenceException(String message) {
    super(message);
  }

  public PersistenceException(String message, Throwable cause) {
    super(message, cause);
  }

  public PersistenceException(Throwable cause) {
    super(cause);
  }
}
