package codes.mark.geilematte

sealed trait MattePermissions
object MattePermissions {
  case object MatteAdmin  extends MattePermissions
  case object MatteEditor extends MattePermissions
  case object MatteUser   extends MattePermissions
}
