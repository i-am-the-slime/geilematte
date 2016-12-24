package codes.mark.geilematte

import codes.mark.geilematte.facades.TwinBcrypt

/**
  * Created by meibes on 14.12.16.
  */
object BCrypt {
  def encryptWithSalt(s:String, salt:Salt) = {
    PasswordWithSalt(EncryptedPassword(
      TwinBcrypt.hashSync(s, salt.str)),
      salt
    )
  }
  def encrypt(s:String):PasswordWithSalt = {
    val salt = Salt(TwinBcrypt.genSalt(10))
    PasswordWithSalt(EncryptedPassword(
      TwinBcrypt.hashSync(s, salt.str)),
      salt
    )
  }
}
