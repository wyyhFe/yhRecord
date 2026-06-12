<script setup lang="ts">
import { ref, onMounted } from "vue";
import { message } from "@/utils/message";
import {
  getKnowledgeBaseList,
  searchKnowledgeChunks,
  askRag,
  type KnowledgeBaseVO,
  type ChunkHitVO
} from "@/api/knowledge";

defineOptions({ name: "KnowledgeRag" });

const activeTab = ref("ask");
const loading = ref(false);

// 知识库选择
const baseList = ref<KnowledgeBaseVO[]>([]);
const selectedBaseId = ref<number | null>(null);

// 搜索 / 问答
const queryText = ref("");
const topK = ref(5);

// 搜索结果
const searchResult = ref<ChunkHitVO[]>([]);
const searchTotal = ref(0);

// RAG 问答结果
const ragAnswer = ref("");
const ragSources = ref<ChunkHitVO[]>([]);

onMounted(async () => {
  try {
    const { data } = await getKnowledgeBaseList();
    baseList.value = data || [];
    if (baseList.value.length > 0) {
      selectedBaseId.value = baseList.value[0].id;
    }
  } catch {
    // ignore
  }
});

async function handleSearch() {
  if (!selectedBaseId.value) {
    message("请选择知识库", { type: "warning" });
    return;
  }
  if (!queryText.value.trim()) {
    message("请输入查询内容", { type: "warning" });
    return;
  }
  loading.value = true;
  try {
    const { data } = await searchKnowledgeChunks(
      selectedBaseId.value,
      queryText.value.trim(),
      topK.value
    );
    searchResult.value = data?.hits || [];
    searchTotal.value = data?.totalHits || 0;
  } catch (e: any) {
    message(e?.message || "检索失败", { type: "error" });
  } finally {
    loading.value = false;
  }
}

async function handleAsk() {
  if (!selectedBaseId.value) {
    message("请选择知识库", { type: "warning" });
    return;
  }
  if (!queryText.value.trim()) {
    message("请输入问题", { type: "warning" });
    return;
  }
  loading.value = true;
  ragAnswer.value = "";
  ragSources.value = [];
  try {
    const { data } = await askRag({
      knowledgeBaseId: selectedBaseId.value,
      query: queryText.value.trim(),
      topK: topK.value
    });
    ragAnswer.value = data?.answer || "";
    ragSources.value = data?.references || [];
  } catch (e: any) {
    message(e?.message || "问答请求失败", { type: "error" });
  } finally {
    loading.value = false;
  }
}

function onQuery() {
  if (activeTab.value === "search") {
    handleSearch();
  } else {
    handleAsk();
  }
}
</script>

<template>
  <div class="main">
    <el-card shadow="hover">
      <template #header>
        <span class="text-lg font-medium">RAG 分析</span>
      </template>

      <el-row :gutter="16" class="mb-4">
        <el-col :span="8">
          <el-form-item label="知识库">
            <el-select v-model="selectedBaseId" placeholder="选择知识库" class="w-full">
              <el-option
                v-for="b in baseList"
                :key="b.id"
                :label="b.name"
                :value="b.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="4">
          <el-form-item label="TopK">
            <el-input-number v-model="topK" :min="1" :max="20" />
          </el-form-item>
        </el-col>
      </el-row>

      <div class="flex gap-2 mb-4">
        <el-input
          v-model="queryText"
          placeholder="输入查询内容或问题..."
          clearable
          @keyup.enter="onQuery"
        />
        <el-button type="primary" :loading="loading" @click="onQuery">
          {{ activeTab === 'search' ? '检索' : '问答' }}
        </el-button>
      </div>

      <el-tabs v-model="activeTab" class="mt-2">
        <!-- RAG 问答 -->
        <el-tab-pane label="RAG 问答" name="ask">
          <div v-if="ragAnswer" class="mb-4">
            <el-alert
              title="回答"
              type="success"
              :description="ragAnswer"
              :closable="false"
              show-icon
            />
          </div>

          <div v-if="ragSources.length > 0">
            <h4 class="text-sm text-gray-500 mb-2">参考来源（{{ ragSources.length }} 条）</h4>
            <div v-for="(src, i) in ragSources" :key="i" class="mb-2">
              <el-card shadow="never" class="!border-gray-200">
                <div class="text-xs text-gray-400 mb-1">
                  #{{ src.chunkId }} · {{ src.documentTitle }} · 切片 {{ src.chunkIndex }}
                  <span class="ml-2">得分: {{ src.relevanceScore?.toFixed(2) }}</span>
                </div>
                <p class="text-sm whitespace-pre-wrap">{{ src.content }}</p>
              </el-card>
            </div>
          </div>

          <el-empty v-if="!loading && !ragAnswer && !ragSources.length" description="输入问题后点击「问答」开始" />
        </el-tab-pane>

        <!-- 语义检索 -->
        <el-tab-pane label="语义检索" name="search">
          <div v-if="searchResult.length > 0" class="mb-2 text-sm text-gray-500">
            共命中 {{ searchTotal }} 条结果
          </div>

          <div v-for="(hit, i) in searchResult" :key="i" class="mb-2">
            <el-card shadow="never" class="!border-gray-200">
              <div class="text-xs text-gray-400 mb-1">
                #{{ hit.chunkId }} · {{ hit.documentTitle }} · 切片 {{ hit.chunkIndex }}
                <span class="ml-2">得分: {{ hit.relevanceScore?.toFixed(2) }}</span>
              </div>
              <p class="text-sm whitespace-pre-wrap">{{ hit.content }}</p>
            </el-card>
          </div>

          <el-empty v-if="!loading && !searchResult.length" description="输入查询后点击「检索」开始" />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>
