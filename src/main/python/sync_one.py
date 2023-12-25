import g4f
import sys
g4f.debug.logging = True
g4f.debug.check_version = False
outfilename = sys.argv[1]
question = sys.argv[2]
myproxy = sys.argv[3]


def pr(string):
    f = open(outfilename, "w", encoding="utf-8")
    f.write(string)
    f.close()

try:
    pr(g4f.ChatCompletion.create(
        model=g4f.models.gpt_4,
        messages=[{"role": "user", "content": question}],
        timeout=10
    ))
    exit(0)
except:
    pass

try:
    pr(g4f.ChatCompletion.create(
        model=g4f.models.gpt_4,
        messages=[{"role": "user", "content": question}],
        proxy=myproxy,
        timeout=10
    ))
    exit(0)
except:
    pass

pr(g4f.ChatCompletion.create(
    model=g4f.models.default,
    messages=[{"role": "user", "content": question}],
    proxy=myproxy,
    timeout=10
))
