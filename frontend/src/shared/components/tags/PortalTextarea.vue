<script setup lang="ts">
const model = defineModel<string>({ default: "" });

withDefaults(defineProps<{
  id?: string;
  label?: string;
  placeholder?: string;
  disabled?: boolean;
  rows?: number;
  maxlength?: number;
  error?: string;
  helpText?: string;
}>(), {
  id: undefined,
  label: "",
  placeholder: "",
  disabled: false,
  rows: 4,
  maxlength: undefined,
  error: "",
  helpText: "",
});
</script>

<template>
  <label class="portal-field">
    <span v-if="label" class="portal-field-label">{{ label }}</span>
    <textarea
      :id="id"
      v-model="model"
      class="portal-control portal-textarea"
      :placeholder="placeholder"
      :disabled="disabled"
      :rows="rows"
      :maxlength="maxlength"
    />
    <span v-if="error" class="portal-field-error">{{ error }}</span>
    <span v-else-if="helpText" class="portal-field-help">{{ helpText }}</span>
  </label>
</template>
