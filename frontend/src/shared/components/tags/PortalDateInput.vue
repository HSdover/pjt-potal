<script setup lang="ts">
const model = defineModel<string>({ default: "" });

withDefaults(defineProps<{
  id?: string;
  label?: string;
  disabled?: boolean;
  min?: string;
  max?: string;
  error?: string;
  helpText?: string;
}>(), {
  id: undefined,
  label: "",
  disabled: false,
  min: undefined,
  max: undefined,
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
      class="portal-control portal-date-control"
      type="date"
      :disabled="disabled"
      :min="min"
      :max="max"
    />
    <span v-if="error" class="portal-field-error">{{ error }}</span>
    <span v-else-if="helpText" class="portal-field-help">{{ helpText }}</span>
  </label>
</template>
