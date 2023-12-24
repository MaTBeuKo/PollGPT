import pkg_resources
pkg_resources.require("g4f==0.1.9.3")
import g4f
import asyncio
import sys

question = sys.argv[1]
_providers = [
    g4f.Provider.ChatBase,
    g4f.Provider.Bing,
    g4f.Provider.GptGo,
    g4f.Provider.You,
    g4f.Provider.Hashnode,
    g4f.Provider.GptForLove,
    g4f.Provider.GPTalk,
    g4f.Provider.FreeGpt,
    g4f.Provider.FakeGpt
]

async def run_provider(provider: g4f.Provider.BaseProvider):
        response = await g4f.ChatCompletion.create_async(
            model=g4f.models.default,
            messages=[{"role": "user", "content": question}],
            provider=provider,
            proxy=""
        )
        return f"{provider.__name__}:" + "MMMstart" + response + "MMM"


async def run_all():
    tasks = [asyncio.create_task(run_provider(provider)) for provider in _providers]
    while len(tasks) > 0:
        done, _ = await asyncio.wait(tasks, return_when=asyncio.FIRST_COMPLETED)
        for task in done:
            tasks.remove(task)

            if task.exception() is None:
                # Task completed successfully, cancel remaining tasks
                print(task.result())
                for remaining_task in tasks:
                    tasks.remove(remaining_task)
                    if remaining_task not in done:
                        remaining_task.cancel()
                tasks.clear()
                break
    # Check for successful completion without exceptions


    # Wait for the canceled tasks to finish (ignore exceptions)
    await asyncio.gather(*tasks, return_exceptions=True)


# Use asyncio.run to run the async function
asyncio.run(run_all())
