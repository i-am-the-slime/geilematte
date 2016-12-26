package codes.mark.geilematte

final case class SessionInfo(unboxed:String) extends AnyVal

final case class SessionCheck(uid:UserId, session:SessionInfo)
