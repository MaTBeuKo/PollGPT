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
    print("Trying FreeGPT with proxy: " + myproxy)
    pr(g4f.ChatCompletion.create(
        provider=g4f.Provider.ChatBase,
        messages=[{"role": "user", "content": question}],
        proxy=myproxy,
        timeout=6
    ))
    exit(0)
except:
    pass

try:
    print("Trying gpt 4 with proxy: " + myproxy)
    pr(g4f.ChatCompletion.create(
        model=g4f.models.gpt_4,
        messages=[{"role": "user", "content": question}],
        proxy=myproxy,
        timeout=6
    ))
    exit(0)
except:
    pass
print("Trying gpt 3.5 with proxy: " + myproxy)
pr(g4f.ChatCompletion.create(
    model=g4f.models.default,
    messages=[{"role": "user", "content": question}],
    proxy=myproxy,
    timeout=6
))
