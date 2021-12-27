#hussien 
#afnan  

#Running the program : 
    1) download the source files compile them with 
        # mvn compile && mvn package
       
    2) upload the jar files of Worker and Manager to s3 under huss bucket wich we used to downlaod jar in our code 
    3) run the Local application with input files fllow by output files and n and "terminate".
    4) the local appliction will tell when every output file is ready .



▪Did you think for more than 2 minutes about security? Do not send your credentials in plain text!
- Yes, The credentials are sent dynamically through the user data, meaning it will work on any computer with proper ~/.aws/credentials file, and cannot be seen.
▪Did you think about scalability? Will your program work properly when 1 million clients connected at the same time? How about 2 million? 1 billion? 
Scalability is very important aspect of the system, be sure it is scalable!

- Yes, The Manager works on each local file seperatly using threads, each local recieves one thread.
scalability is our most important therefore the manager multithreaded that can scalable as many threades as possible.

▪What about persistence? What if a node dies? What if a node stalls for a while? Have you taken care of all possible outcomes in the system? Think of more possible issues that might arise from failures. What did you do to solve it? What about broken communications? Be sure to handle all fail-cases!

- If a worker dies, other workers can complete the message because he will let go of it,
 each worker has a seperate thread to keep the message invisible so no two workers can work on the same message.

▪Threads in your application, when is it a good idea? When is it bad? Invest time to think about threads in your application!
- I used threads.
  each client has his own thread in the manager, using cachedpool we will use threads only when we need.

▪Did you run more than one client at the same time? Be sure they work properly, and finish properly, and your results are correct.

- it works as expected.

▪Do you understand how the system works? Do a full run using pen and paper, draw the different parts and the communication that happens between them.
- All steps that are required in the assignment have been done correctly.

1 Local checks if the manager is active, activates one if not.
2 Local uploads file to S3
3 Local sends one file's location to SQS queue
4 Manager fetches the location of the file
5 Manager downloads the file
6 Manager sends the tasks to the SQS queue
7 Manager activates worker's nodes according to the 'n'
8 Workers recieve the manager's message from queue, and replies to the reply queue that has a unique name
9 Manager recieves the workers messages from the reply queue
10 Manager makes a summary file and uploads to the S3
11 Manager alerts the local about the file's location
12 Local downloads the file and make an HTML file out of it

▪Did you manage the termination process? Be sure all is closed once requested!

- Yes, the manager terminates himself when he's done terminating all workers

▪Did you take in mind the system limitations that we are using? Be sure to use it to its fullest!
- Yes, I'm using T2_Small instead of Micro to get an additional 1GB for the NLP libraries, using cached thread pool also helps.

▪Are all your workers working hard? Or some are slacking? Why?
- Each worker gets one message, so he won't have to work 'too hard', and all workers will get to work on a message. None are waiting doing nothing.
▪Is your manager doing more work than he's supposed to? Have you made sure each part of your system has properly defined tasks? Did you mix their tasks?
 no.

