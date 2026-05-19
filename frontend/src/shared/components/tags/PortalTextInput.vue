<script setup lang="ts">
const model = defineModel<string>({ default: "" });

withDefaults(defineProps<{
  id?: string;
  label?: string;
  placeholder?: string;
  disabled?: boolean;
  maxlength?: number;
  error?: string;
  helpText?: string;
}>(), {
  id: undefined,
  label: "",
  placeholder: "",
  disabled: false,
  maxlength: undefined,
  error: "",
  helpText: "",
});
</script>

<template>
  <label class="portal-field">
    <span v-if="label" class="portal-field-label">{{ label }}</span>
    <input
      :id="id"
      v-model="model"
      class="portal-control"
      type="text"
      :placeholder="placeholder"
      :disabled="disabled"
      :maxlength="maxlength"
    />
    <span v-if="error" class="portal-field-error">{{ error }}</span>
    <span v-else-if="helpText" class="portal-field-help">{{ helpText }}</span>
  </label>
</template>
