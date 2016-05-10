# toggl-logger

A command line utility for starting and stopping Toggl entries according to a specific process.

```
Usage: toggl-logger [options]

Options:
      --start CID      Start a time entry corresponding to a CID - returns the ENTRY_ID
      --stop ENTRY_ID  Stop an existing time entry
  -g, --tag TAG        Tag applied to access token
  -n, --name NAME      The name of the client
  -t, --token TOKEN    Your toggl access token
  -d, --desc DESC      A description for your time entry
```

## Example

```bash
# start
$ ENTRY_ID=#(java -jar toggl-logger.jar --start <CID> --g <TAG> -n <NAME> -d <DESC> -t <TOKEN>)

# stop
$ java -jar toggl-logger.jar --stop $ENTRY_ID -t <TOKEN>
```
