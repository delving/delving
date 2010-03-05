import sys
import os
import gzip


KEEP_OLD_UNZIPS = True

def walk_a_path(top_path):
    os.path.walk(top_path, expand_all_gzips, None)

def expand_all_gzips(arg, dir_name, names):
    b_printed = False
    for source_name in names:
        base_name, ext = os.path.splitext(source_name)
        if ext == '.gz':
            if not b_printed:
                b_printed = True
                print dir_name, ' ',
            dest_file = os.path.join(dir_name, base_name)

            if os.path.exists(dest_file):
                if KEEP_OLD_UNZIPS:
                    continue
                sys.stdout.write('e') ; sys.stdout.flush()
                os.remove(dest_file)

            sys.stdout.write('r') ; sys.stdout.flush()
            f_in = gzip.open(os.path.join(dir_name, source_name))
            data = f_in.read()
            f_in.close()

            sys.stdout.write('w') ; sys.stdout.flush()
            f_out = open(dest_file,'wb')
            f_out.write(data)
            f_out.close()
            sys.stdout.write(' ') ; sys.stdout.flush()
    if b_printed:
        print 'done!'


if __name__ == '__main__':
    print
    print 'This tool expands all found gzips, keeping the source gzip file'
    if not len(sys.argv) > 1:
        print '***   First param must be top path to expand'
        sys.exit(1)

    top_path = sys.argv[1]
    walk_a_path(top_path)