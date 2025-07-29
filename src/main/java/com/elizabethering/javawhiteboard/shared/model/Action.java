package com.elizabethering.javawhiteboard.shared.model;

import java.io.Serializable;

/**
 * 一个标记接口，所有客户端和服务器之间传递的动作指令都应实现此接口。
 * 它继承了 Serializable，确保所有动作都可被序列化。
 */
public interface Action extends Serializable {
}
