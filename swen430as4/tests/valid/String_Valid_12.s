
	.text
wl_main:
	pushq %rbp
	movq %rsp, %rbp
	subq $16, %rsp
	movq $48, %rax
	movq %rax, %rdi
	call malloc
	movq %rax, %rax
	movq $5, %rbx
	movq %rbx, 0(%rax)
	movq %rax, -8(%rbp)
	movq $72, %rbx
	movq %rbx, 8(%rax)
	movq $101, %rbx
	movq %rbx, 16(%rax)
	movq $108, %rbx
	movq %rbx, 24(%rax)
	movq $108, %rbx
	movq %rbx, 32(%rax)
	movq $111, %rbx
	movq %rbx, 40(%rax)
	movq -8(%rbp), %rax
	movq -16(%rbp), %rbx
	movq $0, %rcx
	shlq %rcx
	shlq %rcx
	shlq %rcx
	addq $8, %rcx
	addq %rcx, %rbx
	movq 0(%rbx), %rbx
	jmp label416
	movq $1, %rax
	jmp label417
label416:
	movq $0, %rax
label417:
	movq %rax, %rdi
	call assertion
	movq -8(%rbp), %rax
	movq -16(%rbp), %rbx
	movq $1, %rcx
	shlq %rcx
	shlq %rcx
	shlq %rcx
	addq $8, %rcx
	addq %rcx, %rbx
	movq 0(%rbx), %rbx
	movq $1, %rax
	jmp label419
label418:
	movq $0, %rax
label419:
	movq %rax, %rdi
	call assertion
label415:
	movq %rbp, %rsp
	popq %rbp
	ret
	.globl main
main:
	pushq %rbp
	call wl_main
	popq %rbp
	movq $0, %rax
	ret

	.data
