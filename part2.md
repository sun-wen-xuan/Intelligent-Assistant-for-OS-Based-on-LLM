## 阶段二：基础模型加载与量化

- 目标：
  - 加载主流8B级别大模型，支持高效量化推理。
- 主要成果：
  - 成功加载 `unsloth/DeepSeek-R1-Distill-Llama-8B`，采用4bit量化，显存占用降至20GB以内。
  - 初始化Tokenizer，实现中英文混合自然语言理解。
  - 验证模型推理速度与资源占用，满足Colab/本地服务器部署需求。
- 代码：
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
- 运行结果：
  