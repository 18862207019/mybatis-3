package org.apache.ibatis.exceptions;

import org.apache.ibatis.executor.ErrorContext;

/**
 * 异常工厂
 */
public class ExceptionFactory {

  private ExceptionFactory() {

  }

  /**
   * 包装异常成 PersistenceException
   *
   * @param message 消息
   * @param e 发生的异常
   * @return PersistenceException
   */
  public static RuntimeException wrapException(String message, Exception e) {
    return new PersistenceException(ErrorContext.instance().message(message).cause(e).toString(), e);
  }

}
