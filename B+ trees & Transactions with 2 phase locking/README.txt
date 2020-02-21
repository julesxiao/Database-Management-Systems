HW1: You will first construct the basic building block of a table: Tuples. Next, you will implement a simple Catalog that keeps track of what tables are currently in the database and provides a way to access them. Finally, you will implement HeapFiles which are the physical representation of the data in our database.

HW2: You will implement relational operations as discussed in class. Your first task will be to implement the operations themselves. You will then use a parser to help translate SQL queries into these relational operations, effectively allowing users to query the data stored on your server.

HW3: You will implement B+ trees as discussed in class.You will also have the option to integrate these trees with the rest of your DBMS.

HW4: you will implement locking that can be used with transactions. These transactions will use the two phase locking routine that we have discussed in class. These transactions are specifically expected to implement strict two phase locking, meaning that they will acquire all locks before performing any modifications on data. Locks should generally be kept until the transaction is complete (either aborted or committed), though it may be possible to release some locks earlier than that.

Note that you are not being tasked with creating transactions themselves. We are simply providing the methods that transactions would use to access data. It is then the transaction's responsibility to use these methods properly.
