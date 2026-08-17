package com.typesafe.tools.mima.core

object MunitMimaUtils {

  def isPublic(obj: MemberInfo): Boolean =
    (null ne obj) && !obj.nonAccessible && isPublic(obj.owner)

  // `outer` is NoClass for a top-level class, and NoClass carries no flags, so
  // it must count as public or every top-level class is deemed inaccessible.
  def isPublic(obj: ClassInfo, ref: AnyRef = null): Boolean = (obj eq ref) ||
    (obj eq NoClass) || (null ne obj) && obj.scopedPrivateSuff.isEmpty &&
    obj.isPublic && isPublic(obj.module, obj) && isPublic(obj.outer, obj)

}
