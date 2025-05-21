package com.typesafe.tools.mima.core

object MunitMimaUtils {

  def isPublic(obj: MemberInfo): Boolean =
    (null ne obj) && !obj.nonAccessible && isPublic(obj.owner)

  def isPublic(obj: ClassInfo, ref: AnyRef = null): Boolean = (obj eq ref) ||
    (null ne obj) && obj.scopedPrivateSuff.isEmpty &&
    obj.isPublic && isPublic(obj.module, obj) && isPublic(obj.outer, obj)

}
