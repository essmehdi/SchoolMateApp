package com.github.essmehdi.schoolmate.shared.api

sealed class BaseResponse<T>(val data: T? = null, val message: String? = null, val code: Int? = null) {
  class Success<T>(data: T): BaseResponse<T>(data = data)
  class Loading<T>: BaseResponse<T>()
  class Error<T>(message: String?, code: Int): BaseResponse<T>(message = message, code = code) {
    constructor(code: Int): this(message = null, code = code)
  }
}
