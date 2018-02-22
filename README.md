# Authorisation service  
[Site](https://carcassonne-alpine-meadows.herokuapp.com/sign_up.html)

### Functional  
* Sign up. Takes 3 required arguments: `username`, `email`, `password` and try to create new user. `username` and `email` should be unique. In case of success, sends a letter to the specified email address with a link to the account confirmation.
* Sign in. Takes 2 reqired arguments: `username`, `password` and try to sign in. In case of success, returns token, that can be used in the future
* Reset password. Takes 2 required arguments: `login`, that can be email or username and `password` - new password. Then sends a letter to user email with a link to change password.
* Send email. Takes 3 arguments: `login`, that can be email or username, `password` - new password, `type`, that can be <i>Sign up letter</i> or <i>Reset password lettter</i>. If type is <i>Sign up letter</i> `login` and `type` are required, otherwise all 3 arguments are required. If type is <i>Sign up letter</i> sends a letter to the specified email address with a link to the account confirmation. So you may use that if you have signed up, but didn't receive letter. If type is <i>Reset password letter</i> sends a letter to user email with a link to change password.


### Database
<img alt="ER Diagram" src="https://pp.userapi.com/c830608/v830608267/8c438/ZIZm_f3nKDQ.jpg" height="178px">
There is no links between tables, because all tables have the same id. Also often `players_reset` and `players_verify` has less entities than in `players`, so finding entitiy will be faster than if all fields were in the same table.
 
