Link to Git-Repository: https://github.com/Foxatdoom/MTCG.git

**start postgres database:**
1. start docker desktop
2. run "swen-postgres" container
3. if not working because port problems :/ ->
   "docker run --name swen-postgres -p 5432:5432 -e POSTGRES_PASSWORD=postgres -d postgres" in cmd

Problems and other infos:
"reload all maven projects" when pom.xml doesnt work

postgres needs schema before we can use the sql script -> public schema benutzen (default)!!!!!!

when "**Address already in use: NET_Bind**" is the problem -> port doesnt work
solution in cmd(admin):
net stop winnat
docker start [container_name]
net start winnat

(WinNAT (Windows Network Address Translation) is a windows operating system service. 
It helps in translating private network addresses into a public address. So, such conflicts 
in ports for docker can be solved by restarting that service.)


