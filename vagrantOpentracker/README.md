This will run the Opentracker BitTorrent tracker server in an Ubuntu VM.

This is the first Vagrant box I've set up myself, so it may not be ideal.  Note that it's set up
 to use "public_network" (i.e. bridge) so that the guest (Ubuntu) has some access to your host OS,
 which seems to be necessary for a tracker?


Prerequisites:
- Vagrant https://www.vagrantup.com/downloads.html
- Virtualbox https://www.virtualbox.org/wiki/Downloads

It may well work with other VM systems, but I haven't tried.

Once those two are installed, open a shell in this folder and execute "vagrant up"

The first time may take 5-10 minutes, and will dump a lot to your console, including compilation warnings.
  The next time it will be quicker.

It should end simply with this:

```
==> default: cc -o opentracker.debug opentracker.debug.o trackerlogic.debug.o scan_urlencoded_query.debug.o ot_mutex.debug.o ot_stats.debug.o ot_vector.debug.o ot_clean.debug.o ot_udp.debug.o ot_iovec.debug.o ot_fullscrape.debug.o ot_accesslist.debug.o ot_http.debug.o ot_livesync.debug.o ot_rijndael.debug.o -L../libowfat -lowfat -pthread -lpthread -lz
```

Now log into the box with "vagrant ssh".

```
$ vagrant ssh
Welcome to Ubuntu 12.04.5 LTS (GNU/Linux 3.2.0-86-virtual x86_64)

 * Documentation:  https://help.ubuntu.com/

  System information as of Fri Jun 19 16:26:01 UTC 2015

  System load:  0.16              Processes:           66
  Usage of /:   3.4% of 39.37GB   Users logged in:     0
  Memory usage: 13%               IP address for eth0: 10.0.2.15
  Swap usage:   0%                IP address for eth1: 192.168.1.123
```

Remember the IP address after "IP address for eth1".

To start Opentracker:

```
vagrant@vagrant-ubuntu-precise-64:~$ ./opentracker/opentracker.debug
Binding socket type TCP to address [0.0.0.0]:6969... success.
Binding socket type UDP to address [0.0.0.0]:6969... success.
 installing 0 workers on udp socket -1
```

It will sit like that until you Ctrl-C.

You should now be able to hit it with your browser in your host OS:  http://192.168.1.123:6969/stats

```
0
0
opentracker serving 0 torrents
opentracker
```


