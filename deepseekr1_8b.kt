# 安装 unsloth 包。unsloth 是一个用于微调大型语言模型（LLM）的工具，可以让模型运行更快、占用更少内存。
!pip install unsloth

# 卸载当前已安装的 unsloth 包（如果已安装），然后从 GitHub 的源代码安装最新版本。
# 这样可以确保我们使用的是最新功能和修复。
!pip uninstall unsloth -y && pip install --upgrade --no-cache-dir --no-deps git+https://github.com/unslothai/unsloth.git

# 安装 bitsandbytes 和 unsloth_zoo 包。
# bitsandbytes 是一个用于量化和优化模型的库，可以帮助减少模型占用的内存。
# unsloth_zoo 可能包含了一些预训练模型或其他工具，方便我们使用。
!pip install bitsandbytes unsloth_zoo
from unsloth import FastLanguageModel  # 导入FastLanguageModel类，用来加载和使用模型
import torch  # 导入torch工具，用于处理模型的数学运算

max_seq_length = 2048  # 设置模型处理文本的最大长度，相当于给模型设置一个“最大容量”
dtype = None  # 设置数据类型，让模型自动选择最适合的精度
load_in_4bit = True  # 使用4位量化来节省内存，就像把大箱子压缩成小箱子

# 加载预训练模型，并获取tokenizer工具
model, tokenizer = FastLanguageModel.from_pretrained(
    model_name="unsloth/DeepSeek-R1-Distill-Llama-8B",  # 指定要加载的模型名称
    max_seq_length=max_seq_length,  # 使用前面设置的最大长度
    dtype=dtype,  # 使用前面设置的数据类型
    load_in_4bit=load_in_4bit,  # 使用4位量化
    # token="hf_...",  # 如果需要访问授权模型，可以在这里填入密钥
)
prompt_style = """以下是描述任务的指令，以及提供进一步上下文的输入。
请写出一个适当完成请求的回答。
在回答之前，请仔细思考问题，并创建一个逻辑连贯的思考过程，以确保回答准确无误。

### 指令：
你是一位精通操作系统原理、架构和调试技术的语言助手。
请回答以下与操作系统相关的问题。

### 问题：
{}

### 回答：
<think>{}"""
# 定义提示风格的字符串模板，用于格式化问题

question = "请简述操作系统中进程与线程的区别，并举例说明各自的应用场景。"
# 定义具体的操作系统领域问题
FastLanguageModel.for_inference(model)
# 准备模型以进行推理

inputs = tokenizer([prompt_style.format(question, "")], return_tensors="pt").to("cuda")
# 使用 tokenizer 对格式化后的问题进行编码，并移动到 GPU

outputs = model.generate(
    input_ids=inputs.input_ids,
    attention_mask=inputs.attention_mask,
    max_new_tokens=1200,
    use_cache=True,
)
# 使用模型生成回答

response = tokenizer.batch_decode(outputs)
# 解码模型生成的输出为可读文本

print(response[0])
# 打印生成的回答部分
# 定义一个用于格式化提示的多行字符串模板
train_prompt_style = """以下是描述任务的指令，以及提供进一步上下文的输入。
请写出一个适当完成请求的回答。
在回答之前，请仔细思考问题，并创建一个逻辑连贯的思考过程，以确保回答准确无误。

### 指令：
你是一位精通操作系统原理、体系结构、调试技术的操作系统专家。
请回答以下操作系统领域的问题。

### 问题：
{}

### 回答：
<思考>
{}
</思考>
{}"""
# 定义结束标记（EOS_TOKEN），用于指示文本的结束
EOS_TOKEN = tokenizer.eos_token  # 必须添加结束标记

# 导入数据集加载函数
from datasets import load_dataset
# 加载指定的数据集，选择操作系统领域相关的中文问答数据
dataset = load_dataset("sapphire1234/os-qa", 'default', split = "train[0:2480]", trust_remote_code=True)
# 打印数据集的列名，查看数据集中有哪些字段
print(dataset.column_names)
# 定义一个函数，用于格式化数据集中的每条记录
def formatting_prompts_func(examples):
    # 从数据集中提取问题、复杂思考过程和回答
    inputs = examples["instruction"]
    cots = examples["input"]
    outputs = examples["output"]
    texts = []  # 用于存储格式化后的文本
    # 遍历每个问题、思考过程和回答，进行格式化
    for input, cot, output in zip(inputs, cots, outputs):
        # 使用字符串模板插入数据，并加上结束标记
        text = train_prompt_style.format(input, cot, output) + EOS_TOKEN
        texts.append(text)  # 将格式化后的文本添加到列表中
    return {
        "text": texts,  # 返回包含所有格式化文本的字典
    }

dataset = dataset.map(formatting_prompts_func, batched = True)
dataset["text"][0]
FastLanguageModel.for_training(model)

model = FastLanguageModel.get_peft_model(
    model,  # 传入已经加载好的预训练模型
    r = 16,  # 设置 LoRA 的秩，决定添加的可训练参数数量
    target_modules = ["q_proj", "k_proj", "v_proj", "o_proj",  # 指定模型中需要微调的关键模块
                      "gate_proj", "up_proj", "down_proj"],
    lora_alpha = 16,  # 设置 LoRA 的超参数，影响可训练参数的训练方式
    lora_dropout = 0,  # 设置防止过拟合的参数，这里设置为 0 表示不丢弃任何参数
    bias = "none",    # 设置是否添加偏置项，这里设置为 "none" 表示不添加
    use_gradient_checkpointing = "unsloth",  # 使用优化技术节省显存并支持更大的批量大小
    random_state = 3407,  # 设置随机种子，确保每次运行代码时模型的初始化方式相同
    use_rslora = False,  # 设置是否使用 Rank Stabilized LoRA 技术，这里设置为 False 表示不使用
    loftq_config = None,  # 设置是否使用 LoftQ 技术，这里设置为 None 表示不使用
)
from trl import SFTTrainer  # 导入 SFTTrainer，用于监督式微调
from transformers import TrainingArguments  # 导入 TrainingArguments，用于设置训练参数
from unsloth import is_bfloat16_supported  # 导入函数，检查是否支持 bfloat16 数据格式

trainer = SFTTrainer(  # 创建一个 SFTTrainer 实例
    model=model,  # 传入要微调的模型
    tokenizer=tokenizer,  # 传入 tokenizer，用于处理文本数据
    train_dataset=dataset,  # 传入训练数据集
    dataset_text_field="text",  # 指定数据集中文本字段的名称
    max_seq_length=max_seq_length,  # 设置最大序列长度
    dataset_num_proc=2,  # 设置数据处理的并行进程数
    packing=False,  # 是否启用打包功能（这里设置为 False，打包可以让训练更快，但可能影响效果）
    args=TrainingArguments(  # 定义训练参数
        per_device_train_batch_size=2,  # 每个设备（如 GPU）上的批量大小
        gradient_accumulation_steps=4,  # 梯度累积步数，用于模拟大批次训练
        warmup_steps=5,  # 预热步数，训练开始时学习率逐渐增加的步数
        max_steps=75,  # 最大训练步数
        learning_rate=2e-4,  # 学习率，模型学习新知识的速度
        fp16=not is_bfloat16_supported(),  # 是否使用 fp16 格式加速训练（如果环境不支持 bfloat16）
        bf16=is_bfloat16_supported(),  # 是否使用 bfloat16 格式加速训练（如果环境支持）
        logging_steps=1,  # 每隔多少步记录一次训练日志
        optim="adamw_8bit",  # 使用的优化器，用于调整模型参数
        weight_decay=0.01,  # 权重衰减，防止模型过拟合
        lr_scheduler_type="linear",  # 学习率调度器类型，控制学习率的变化方式
        seed=3407,  # 随机种子，确保训练结果可复现
        output_dir="outputs",  # 训练结果保存的目录
        report_to="none",  # 是否将训练结果报告到外部工具（如 WandB），这里设置为不报告
    ),
)
print(question) # 打印前面的问题
# 将模型切换到推理模式，准备回答问题
FastLanguageModel.for_inference(model)

# 将问题转换成模型能理解的格式，并发送到 GPU 上
inputs = tokenizer([prompt_style.format(question, "")], return_tensors="pt").to("cuda")

# 让模型根据问题生成回答，最多生成 4000 个新词
outputs = model.generate(
    input_ids=inputs.input_ids,  # 输入的数字序列
    attention_mask=inputs.attention_mask,  # 注意力遮罩，帮助模型理解哪些部分重要
    max_new_tokens=4000,  # 最多生成 4000 个新词
    use_cache=True,  # 使用缓存加速生成
)

# 将生成的回答从数字转换回文字
response = tokenizer.batch_decode(outputs)

# 打印回答
print(response[0])
# 导入 Google Colab 的 userdata 模块，用于访问用户数据
from google.colab import userdata

# 从 Google Colab 用户数据中获取 Hugging Face 的 API 令牌
HUGGINGFACE_TOKEN = userdata.get('Chawen')

# 将模型保存为 8 位量化格式（Q8_0）
# 这种格式文件小且运行快，适合部署到资源受限的设备
if True: model.save_pretrained_gguf("model", tokenizer,)

# 将模型保存为 16 位量化格式（f16）
# 16 位量化精度更高，但文件稍大
if False: model.save_pretrained_gguf("model_f16", tokenizer, quantization_method = "f16")

# 将模型保存为 4 位量化格式（q4_k_m）
# 4 位量化文件最小，但精度可能稍低
if False: model.save_pretrained_gguf("model", tokenizer, quantization_method = "q4_k_m")
# 导入 Hugging Face Hub 的 create_repo 函数，用于创建一个新的模型仓库
from huggingface_hub import create_repo

# 在 Hugging Face Hub 上创建一个新的模型仓库
create_repo("Chawen/os",  token=HUGGINGFACE_TOKEN, exist_ok=True)

# 将模型和分词器上传到 Hugging Face Hub 上的仓库
model.push_to_hub_gguf("Chawen/os",tokenizer, token=HUGGINGFACE_TOKEN)
