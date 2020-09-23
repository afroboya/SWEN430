
	.text
wl_f:
	pushq %rbp
	movq %rsp, %rbp
	subq $48, %rsp
	movq $0, %rax
	movq %rax, -8(%rbp)
	movq $0, %rax
	movq %rax, -16(%rbp)
label394:
	movq -16(%rbp), %rax
	cmpq %rbx, %rax
	jge label395
	movq 32(%rbp), %rax
	movq -16(%rbp), %rbx
	shlq %rbx
	shlq %rbx
	shlq %rbx
	addq $8, %rbx
	addq %rbx, %rax
	movq 0(%rax), %rbx
	movq %rbx, -24(%rbp)
	movq $0, %rax
	movq %rax, -32(%rbp)
	movq $0, %rax
	movq %rax, -40(%rbp)
label396:
	movq -40(%rbp), %rax
	cmpq %rbx, %rax
	jge label397
	movq -24(%rbp), %rax
	movq -40(%rbp), %rbx
	shlq %rbx
	shlq %rbx
	shlq %rbx
	addq $8, %rbx
	addq %rbx, %rax
	movq 0(%rax), %rax
	movq 24(%rbp), %rbx
	cmpq %rax, %rbx
	jnz label398
	movq $1, %rax
	movq %rax, -32(%rbp)
	jmp label398
label398:
	movq -40(%rbp), %rax
	movq $1, %rbx
	addq %rbx, %rax
	movq %rax, -40(%rbp)
	jmp label396
label397:
	movq -32(%rbp), %rax
	cmpq $0, %rax
	jz label400
	jmp label399
label400:
	movq $0, %rax
	movq %rax, 16(%rbp)
	jmp label393
	jmp label399
label399:
	movq -16(%rbp), %rax
	movq $1, %rbx
	addq %rbx, %rax
	movq %rax, -16(%rbp)
	jmp label394
label395:
	movq $1, %rax
	movq %rax, 16(%rbp)
	jmp label393
label393:
	movq %rbp, %rsp
	popq %rbp
	ret
wl_main:
	pushq %rbp
	movq %rsp, %rbp
	subq $32, %rsp
	movq $1, %rax
	movq %rax, 8(%rsp)
	call wl_f
	addq $32, %rsp
	movq -32(%rsp), %rax
	movq %rax, %rdi
	call assertion
	subq $32, %rsp
	movq $1, %rax
	movq %rax, 8(%rsp)
	call wl_f
	addq $32, %rsp
	movq -32(%rsp), %rax
	movq %rax, %rdi
	call assertion
	subq $32, %rsp
	movq $1, %rax
	movq %rax, 8(%rsp)
	call wl_f
	addq $32, %rsp
	movq -32(%rsp), %rax
	movq %rax, %rdi
	call assertion
	subq $32, %rsp
	movq $2, %rax
	movq %rax, 8(%rsp)
	call wl_f
	addq $32, %rsp
	movq -32(%rsp), %rax
	movq %rax, %rdi
	call assertion
	movq %rax, %rdi
	call assertion
	movq %rax, %rdi
	call assertion
	movq %rax, %rdi
	call assertion
label401:
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
