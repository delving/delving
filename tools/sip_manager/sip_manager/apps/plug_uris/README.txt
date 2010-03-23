About the URI plugin



When new MdRecords are created nothing happens with Uris, everything is done by the plugin

Note, below I talk about deleting stuff, if policy so decides deletions can be changed into
cold-storage.


Tasks (processes)


"constructor"
Scan all MdRecords for items with no Uris connected
For each record extract all uris in that record and create uris and join it to a UriSource

"processor"
For each available UriSource loop on all unprocessed Uris
  check this uri and if it is a object try to download and generate thumbnail
  end the Uri in one of the final states to indicate that this record is done for
  the timebeing

"destructor"
Find all Uris where the MdRecord has been deleted, if its an object remove all the
relevant files, then remove the Uri

"Filetree monitor"
Run quarterly or similar, this takes a while :)
For each file, check if there is an Uri refering to this file, if not remove it.






