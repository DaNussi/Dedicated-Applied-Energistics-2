# Cloud based database

There are two options you can choose. One way is to create a free cloud hosted database the other option is to host it yourself. I recommend hosting it yourself because this keeps latency low and your data local. 

--- 
# Cloud Hosted
1. Klick this link [https://app.redislabs.com/#/login](https://app.redislabs.com/#/login) and login.

2. Once you're logged in got to `Databases > Create subscription`. Don't worry everything is free.
+ ![img_3.png](img_3.png)

3. Choose the 'Fixed plan' option and scroll down to where you can select the Region.
+ ![img_5.png](img_5.png)

4. Select the `Region` closest to your minecraft server. The cloud vendor doesn't really matter.
+ ![img_2.png](img_2.png)

5. Select `Single-zone replication` in the `Availability Settings`
+ ![img_4.png](img_4.png)

6. Select the Free plan and give it a name.
+ ![img_7.png](img_7.png)

7. Click on `Create subscription` and the on `New database`!
![img_8.png](img_8.png)


8. Give the database a name. Leave all settings as is and click on `Activate database`.
+ ![img_9.png](img_9.png)

9. Wait for the Database to be crated. This might take a few minutes.
+ ![img_10.png](img_10.png)
+ ![img_11.png](img_11.png)

10. Scroll down to `Public endpoint` and click on `Connect`
+ ![img_12.png](img_12.png)

11. Open the Redis CLI and click the copy button.
+ ![img_14.png](img_14.png)

12. Use the `/dae2 config set <redos-uri>` and paste the uri so it look like this
`/dae2 config set "redis://default:7vDAEYB5rtKoIazj5jSGloarqwudAsdD@redis-13061.c256.us-east-1-2.ec2.cloud.redislabs.com:13061"`. 
Keep in mind you have to remove  the `redis-cli -u ` part.

13. Now you're good to go and start the mod.