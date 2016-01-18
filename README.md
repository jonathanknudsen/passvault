# passvault

*Do not use this unless you fully understand the risks.*

PassVault is a homegrown password keeper. I created it to suit my own needs. It
has not been reviewed by anyone else and might have glaring errors in it.

PassVault uses a single passphrase to protect an encrypted database of keys and
values. The keys and values can be used for storing user names and passwords or
really anything else you would like.

PassVault places selected passwords in the clipboard for easy cutting and
pasting. The entire database is loaded into memory and exists there in
cleartext. If you're serious about security and privacy, you've probably given
up on this project already. Malicious software running on the same machine as
PassVault might very well be able to access the database in memory or pull
passwords off the clipboard.

The code is slapdash and just good enough to work reliably for me. Lately I've
been using this from multiple platforms with a database files stored on my
Google Drive, which means I get access to the same stuff from wherever I am.
