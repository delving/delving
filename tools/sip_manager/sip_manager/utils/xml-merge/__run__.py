import codecs
import os
import sys

if len(sys.argv) < 2:
    print
    print 'Merges all xmlfiles in given path into one'
    print 'Since files are read and written line by line,'
    print 'This will not care about file sizes'
    print
    print 'Syntax is xml-merge source-directory [dest file]'
    print ' if dest file is ommited, the resulting file'
    print ' will be stored in current directory using the final'
    print ' part of source-directory'
    sys.exit(0)


#cwd = os.getcwd()
scan_dir = os.path.abspath(sys.argv[1])


if len(sys.argv) > 2:
    dest_file = os.path.abspath(sys.argv[2])
else:
    dest_file = os.path.split(scan_dir)[-1] + '.xml'

print 'Source', scan_dir
print 'Dest', dest_file

def create_header():
    header = '''<?xml version='1.0' encoding='UTF-8'?>
<metadata xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:europeana="http://www.europeana.eu" xmlns:dcterms="http://purl.org/dc/terms/">\n'''
    f = open(dest_file, 'w')
    f.write(header)
    f.close()

def create_footer():
    f = open(dest_file, 'a')
    f.write('</metadata>\n')
    f.close()

def foo():
    for dirpath, dirnames, filenames in os.walk(scan_dir):
        for filename in filenames:
            if os.path.splitext(filename)[1].lower() != '.xml':
                continue
            process_one(os.path.join(dirpath,filename))


def process_one(source_file):
    short_source = os.path.split(source_file)[1]
    print 'Processing', short_source
    f_in = codecs.open(source_file, 'r', 'utf-8')
    f_out = codecs.open(dest_file, 'a', 'utf-8')
    record = []
    progress = 0
    count = 0
    for line in f_in:
        if line.find('<record>') > -1:
            record = [line]
        elif line.find('</record>') > -1:
            f_out.writelines(record)
            record = []
            progress += 1
        else:
            record.append(line)
        if progress > 1023:
            progress = 0
            count += 1
            print '%s %ik records done' % (short_source, count)
    f_out.close()
    f_in.close()


create_header()
foo()
create_footer()
