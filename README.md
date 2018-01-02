# rcl
Final project of the Computer Networks course of the Computer Science Bachelor's Degree @ University of Pisa.

| <b>Language</b> | <b>Description</b> |
| --------------- | ------------------ | 
| Java | A distributed chat system where users in a [WAN](https://en.wikipedia.org/wiki/Wide_area_network) can register and exchange messages with each others. Each user has the possibility to register, unregister, login, logout, set up permissions in order to be able to receive messages from other users or block them (if a blocked user tries to write a message this will be dropped). If a user is online then the incoming messages addressed to him are immediatly delivered, otherwise they are stored by one of the proxies and delivered when he logs in again. The architecture of the application is composed by a [Java RMI](https://docs.oracle.com/javase/tutorial/rmi/index.html) registry, a server, a set of user agents that manage the requests inserted by connected users, a set of proxies that manage the messages addressed to offline users. | 
