<script setup lang="ts">
import { computed, ref, watch } from "vue";
import StarterKit from "@tiptap/starter-kit";
import { EditorContent, useEditor } from "@tiptap/vue-3";
import { List, Menu, Operation, RefreshLeft, RefreshRight } from "@element-plus/icons-vue";

const model = defineModel<string>({ default: "" });

const emit = defineEmits<{
  blur: [event: FocusEvent];
}>();

const props = withDefaults(defineProps<{
  id?: string;
  label?: string;
  disabled?: boolean;
  minHeight?: number;
  maxlength?: number;
  showCount?: boolean;
  error?: string;
  helpText?: string;
}>(), {
  id: undefined,
  label: "",
  disabled: false,
  minHeight: 220,
  maxlength: undefined,
  showCount: false,
  error: "",
  helpText: "",
});

const editorStyle = computed(() => ({
  "--portal-rich-text-min-height": `${props.minHeight}px`,
}));
const isOverLimit = computed(() => props.maxlength !== undefined && model.value.length > props.maxlength);
const editorStateVersion = ref(0);

const editor = useEditor({
  content: model.value,
  editable: !props.disabled,
  extensions: [
    StarterKit.configure({
      heading: {
        levels: [2, 3],
      },
    }),
  ],
  editorProps: {
    attributes: {
      class: "portal-rich-text-content",
    },
  },
  onUpdate: ({ editor: updatedEditor }) => {
    const html = updatedEditor.getHTML();
    model.value = html === "<p></p>" ? "" : html;
  },
  onBlur: ({ event }) => {
    emit("blur", event as FocusEvent);
  },
  onTransaction: () => {
    editorStateVersion.value += 1;
  },
});

watch(model, (value) => {
  if (!editor.value) {
    return;
  }

  const currentHtml = editor.value.getHTML();
  const normalizedCurrentHtml = currentHtml === "<p></p>" ? "" : currentHtml;
  if (normalizedCurrentHtml !== value) {
    editor.value.commands.setContent(value || "", { emitUpdate: false });
  }
});

watch(() => props.disabled, (disabled) => {
  editor.value?.setEditable(!disabled);
});
</script>

<template>
  <label class="portal-field">
    <span v-if="label" class="portal-field-label">{{ label }}</span>
    <div
      class="portal-rich-text"
      :class="{
        'is-disabled': disabled,
        'is-invalid': Boolean(error) || isOverLimit,
      }"
      :style="editorStyle"
    >
      <div
        class="portal-rich-text-toolbar"
        role="toolbar"
        aria-label="본문 서식"
        :data-editor-state-version="editorStateVersion"
      >
        <button
          type="button"
          class="portal-rich-text-tool"
          :class="{ 'is-active': editor?.isActive('bold') }"
          :disabled="disabled || !editor"
          title="굵게"
          @click="editor?.chain().focus().toggleBold().run()"
        >
          <strong>B</strong>
        </button>
        <button
          type="button"
          class="portal-rich-text-tool"
          :class="{ 'is-active': editor?.isActive('italic') }"
          :disabled="disabled || !editor"
          title="기울임"
          @click="editor?.chain().focus().toggleItalic().run()"
        >
          <em>I</em>
        </button>
        <button
          type="button"
          class="portal-rich-text-tool"
          :class="{ 'is-active': editor?.isActive('heading', { level: 2 }) }"
          :disabled="disabled || !editor"
          title="제목"
          @click="editor?.chain().focus().toggleHeading({ level: 2 }).run()"
        >
          H2
        </button>
        <button
          type="button"
          class="portal-rich-text-tool"
          :class="{ 'is-active': editor?.isActive('heading', { level: 3 }) }"
          :disabled="disabled || !editor"
          title="소제목"
          @click="editor?.chain().focus().toggleHeading({ level: 3 }).run()"
        >
          H3
        </button>

        <span class="portal-rich-text-divider" />

        <button
          type="button"
          class="portal-rich-text-tool"
          :class="{ 'is-active': editor?.isActive('bulletList') }"
          :disabled="disabled || !editor"
          title="글머리 목록"
          @click="editor?.chain().focus().toggleBulletList().run()"
        >
          <List class="portal-rich-text-icon" />
        </button>
        <button
          type="button"
          class="portal-rich-text-tool"
          :class="{ 'is-active': editor?.isActive('orderedList') }"
          :disabled="disabled || !editor"
          title="번호 목록"
          @click="editor?.chain().focus().toggleOrderedList().run()"
        >
          <Menu class="portal-rich-text-icon" />
        </button>
        <button
          type="button"
          class="portal-rich-text-tool"
          :class="{ 'is-active': editor?.isActive('blockquote') }"
          :disabled="disabled || !editor"
          title="인용"
          @click="editor?.chain().focus().toggleBlockquote().run()"
        >
          "
        </button>
        <button
          type="button"
          class="portal-rich-text-tool"
          :class="{ 'is-active': editor?.isActive('codeBlock') }"
          :disabled="disabled || !editor"
          title="코드 블록"
          @click="editor?.chain().focus().toggleCodeBlock().run()"
        >
          &lt;/&gt;
        </button>

        <span class="portal-rich-text-divider" />

        <button
          type="button"
          class="portal-rich-text-tool"
          :disabled="disabled || !editor"
          title="서식 지우기"
          @click="editor?.chain().focus().unsetAllMarks().clearNodes().run()"
        >
          <Operation class="portal-rich-text-icon" />
        </button>
        <button
          type="button"
          class="portal-rich-text-tool"
          :disabled="disabled || !editor || !editor.can().undo()"
          title="실행취소"
          @click="editor?.chain().focus().undo().run()"
        >
          <RefreshLeft class="portal-rich-text-icon" />
        </button>
        <button
          type="button"
          class="portal-rich-text-tool"
          :disabled="disabled || !editor || !editor.can().redo()"
          title="다시실행"
          @click="editor?.chain().focus().redo().run()"
        >
          <RefreshRight class="portal-rich-text-icon" />
        </button>
      </div>

      <EditorContent :id="id" :editor="editor" />
    </div>

    <span
      v-if="showCount && maxlength"
      class="portal-field-help"
      :class="{ 'portal-field-error': isOverLimit }"
    >
      {{ model.length }} / {{ maxlength }}
    </span>
    <span v-if="error" class="portal-field-error">{{ error }}</span>
    <span v-else-if="helpText" class="portal-field-help">{{ helpText }}</span>
  </label>
</template>

<style scoped>
.portal-rich-text {
  overflow: hidden;
  width: 100%;
  border: 1px solid var(--portal-line-strong);
  border-radius: 6px;
  background: #fff;
  transition:
    border-color 0.15s ease,
    box-shadow 0.15s ease,
    background-color 0.15s ease;
}

.portal-rich-text:focus-within {
  border-color: var(--portal-blue);
  box-shadow: 0 0 0 3px rgba(0, 87, 184, 0.12);
}

.portal-rich-text.is-invalid {
  border-color: #dc2626;
}

.portal-rich-text.is-disabled {
  background: #f1f5f9;
}

.portal-rich-text-toolbar {
  display: flex;
  min-height: 36px;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
  border-bottom: 1px solid #e2e8f0;
  background: #f8fafc;
  padding: 4px;
}

.portal-rich-text-tool {
  display: inline-flex;
  width: 28px;
  height: 28px;
  align-items: center;
  justify-content: center;
  border: 1px solid transparent;
  border-radius: 4px;
  background: transparent;
  color: #334155;
  font-size: 12px;
  font-weight: 800;
  line-height: 1;
}

.portal-rich-text-tool:hover:not(:disabled) {
  border-color: #cbd5e1;
  background: #fff;
}

.portal-rich-text-tool.is-active {
  border-color: rgba(0, 87, 184, 0.35);
  background: rgba(0, 87, 184, 0.1);
  color: var(--portal-blue);
}

.portal-rich-text-tool:disabled {
  cursor: not-allowed;
  color: #94a3b8;
}

.portal-rich-text-icon {
  width: 15px;
  height: 15px;
}

.portal-rich-text-divider {
  width: 1px;
  height: 20px;
  margin: 0 3px;
  background: #cbd5e1;
}

:deep(.portal-rich-text-content) {
  min-height: var(--portal-rich-text-min-height);
  padding: 10px 12px;
  color: var(--portal-ink);
  font-size: 13px;
  line-height: 1.7;
  outline: none;
}

:deep(.portal-rich-text-content h2) {
  margin: 0.35rem 0;
  font-size: 1.15rem;
  font-weight: 800;
}

:deep(.portal-rich-text-content h3) {
  margin: 0.35rem 0;
  font-size: 1rem;
  font-weight: 800;
}

:deep(.portal-rich-text-content p) {
  margin: 0.35rem 0;
}

:deep(.portal-rich-text-content ul),
:deep(.portal-rich-text-content ol) {
  margin: 0.4rem 0;
  padding-left: 1.35rem;
}

:deep(.portal-rich-text-content blockquote) {
  margin: 0.5rem 0;
  border-left: 3px solid #cbd5e1;
  color: #475569;
  padding-left: 0.75rem;
}

:deep(.portal-rich-text-content pre) {
  overflow-x: auto;
  border-radius: 6px;
  background: #0f172a;
  color: #e2e8f0;
  padding: 0.75rem;
}

:deep(.portal-rich-text-content code) {
  border-radius: 4px;
  background: #e2e8f0;
  padding: 0.1rem 0.25rem;
}

:deep(.portal-rich-text-content pre code) {
  background: transparent;
  padding: 0;
}
</style>
